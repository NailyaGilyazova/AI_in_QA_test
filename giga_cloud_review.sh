#!/usr/bin/env bash
set -e

AUTH_KEY="ваш_ключ"
MODEL="GigaChat-2-Max"

echo ">>> Получение токена..."
TOKEN_RESPONSE=$(curl -s -L -X POST 'https://ngw.devices.sberbank.ru:9443/api/v2/oauth' \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -H "Accept: application/json" \
  -H "RqUID: e9866ff6-3b94-401d-8f3a-263fe1aedb6c" \
  -H "Authorization: Basic $AUTH_KEY" \
  --data-urlencode "scope=GIGACHAT_API_PERS")


  ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

  if [ -z "$ACCESS_TOKEN" ]; then
      echo "Ошибка: Не удалось получить токен."
      echo "Ответ сервера: $TOKEN_RESPONSE"
      exit 1
  fi

  echo "Токен получен (действует 30 минут)."

CODE="$(cat)"
if [ -z "$CODE" ]; then
  echo "No code selected"
  exit 0
fi

PROMPT=$(cat <<EOF
Ты — senior QA-инженер. Твоя задача — провести критический анализ предоставленного текста (чек-листа или требований).

Ниже дан текст. Твой ответ должен строго следовать структуре:
1. Неопределенности: Что в тексте описано размыто? (Например, что значит "валидные данные"?)
2. Противоречия: Есть ли взаимоисключающие пункты?
3. Недостающая информация: Каких проверок или уточнений не хватает?
4. Рекомендации: Как улучшить этот чек-лист?

ВАЖНО:
- Не пиши общие статьи про тестирование или Shift Left.
- Не придумывай функционал, которого нет в тексте.
- Опирайся ТОЛЬКО на предоставленный текст.

CODE:
$CODE
EOF
)


PROMPT_ESCAPED=$(printf '%s' "$PROMPT" \
  | sed -e 's/\\/\\\\/g' \
        -e 's/"/\\"/g' \
        -e 's/\r//g' \
  | awk 'BEGIN{ORS=""} {gsub(/\t/,"\\t"); if (NR>1) printf "\\n"; print}')

JSON_PAYLOAD="{
  \"model\": \"$MODEL\",
  \"messages\": [
    {
      \"role\": \"user\",
      \"content\": \"$PROMPT_ESCAPED\"
    }
  ]
}"

RESPONSE=$(curl -L -X POST 'https://gigachat.devices.sberbank.ru/api/v1/chat/completions' \
   -H "Content-Type: application/json" \
   -H "Accept: application/json" \
   -H "Authorization: Bearer $ACCESS_TOKEN" \
   -d "$JSON_PAYLOAD")

echo "===== AI REVIEW (CLOUD) ====="
echo "$RESPONSE" | python -c "import sys, json; print(json.load(sys.stdin)['choices'][0]['message']['content'])" 2>/dev/null || echo "$RESPONSE"
echo "============================="
#!/usr/bin/env bash
#
# MovieHub — последовательный прогон всех JMeter-сценариев.
#
# Запуск:
#   bash load-tests/run-all.sh                       # дефолты, локальный бэк
#   HOST=prod.example.com PORT=80 bash run-all.sh    # против другого бэка
#   PLANS="catalog search" bash run-all.sh           # только эти планы
#
# Каждый прогон создаёт results/<TS>/<plan>.jtl + results/<TS>/<plan>-html/index.html
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TS="$(date +%Y%m%d-%H%M%S)"
RESULTS_DIR="$SCRIPT_DIR/results/$TS"
mkdir -p "$RESULTS_DIR"

HOST="${HOST:-localhost}"
PORT="${PORT:-8080}"
PLANS="${PLANS:-catalog search auth review-create}"   # spike/stress по умолчанию пропускаем (длинные)

# абсолютный путь к csv нужен потому что JMeter резолвит относительный от cwd
QUERIES_FILE="$SCRIPT_DIR/data/search-queries.csv"

if ! command -v jmeter >/dev/null 2>&1; then
    echo "ERROR: jmeter не найден в PATH. Установите: brew install jmeter" >&2
    exit 1
fi

echo "=== MovieHub JMeter run @ $TS ==="
echo "Host: $HOST:$PORT"
echo "Plans: $PLANS"
echo "Results: $RESULTS_DIR"
echo

cd "$SCRIPT_DIR"

for plan in $PLANS; do
    plan_file="plans/${plan}.jmx"
    if [[ ! -f "$plan_file" ]]; then
        echo "SKIP $plan — файл $plan_file не найден"
        continue
    fi

    echo "--- $plan ---"
    jmeter -n \
        -t "$plan_file" \
        -l "$RESULTS_DIR/${plan}.jtl" \
        -e -o "$RESULTS_DIR/${plan}-html" \
        -j "$RESULTS_DIR/${plan}.log" \
        -Jhost="$HOST" \
        -Jport="$PORT" \
        -Jqueries="$QUERIES_FILE" \
        ${THREADS:+-Jthreads="$THREADS"} \
        ${RAMPUP:+-Jrampup="$RAMPUP"} \
        ${DURATION:+-Jduration="$DURATION"} \
        2>&1 | tail -25
    echo
done

echo "=== Все отчёты лежат в: $RESULTS_DIR ==="
echo "Открыть HTML дашборд: open $RESULTS_DIR/<plan>-html/index.html"

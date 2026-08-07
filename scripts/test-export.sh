#!/bin/bash
# test-export.sh - Test the export operation on OnMind-XDB
# Requires: server running on localhost:9990, db.export=+

set -e

echo "=== OnMind-XDB Export Test ==="
echo

XDB_BASE_URL="http://localhost:9990"
AUTH_USER="admin"
AUTH_PASS="admin"

# 1. Health check
echo "1. Health check..."
HEALTH=$(curl -s "$XDB_BASE_URL/health")
if echo "$HEALTH" | jq -e '.healthy' > /dev/null 2>&1; then
    echo "  ✓ Server healthy"
else
    echo "  ✗ Server not reachable on $XDB_BASE_URL"
    exit 1
fi
echo

# 2. Export (fire-and-forget → 202)
echo "2. Export..."
RESULT=$(curl -s -u "$AUTH_USER:$AUTH_PASS" -X POST "$XDB_BASE_URL/abc" \
    -H "Content-Type: application/json" \
    -d '{"what":"export","user":"admin"}')

OK=$(echo "$RESULT" | jq -r '.ok // false')
if [ "$OK" != "true" ]; then
    echo "  ✗ Export rejected: $(echo "$RESULT" | jq -r '.message // empty')"
    exit 1
fi

FILE=$(echo "$RESULT" | jq -r '.file')
echo "  ✓ 202 accepted, file: $FILE"

# Wait for background thread
sleep 3
SIZE=$(stat -f%z "$FILE" 2>/dev/null || stat -c%s "$FILE" 2>/dev/null)
echo "  ✓ File ready: $SIZE bytes"
echo

# 3. Verify SQLite tables
echo "3. SQLite content..."
if command -v sqlite3 > /dev/null 2>&1; then
    for TABLE in xykit xykey xyset xyany xydoc; do
        COUNT=$(sqlite3 "$FILE" "SELECT COUNT(*) FROM $TABLE;")
        echo "  $TABLE: $COUNT rows"
    done
else
    echo "  * sqlite3 not installed, skipping"
fi
echo

echo "=== Done ==="
echo "File: $FILE"

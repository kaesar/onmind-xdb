#!/usr/bin/env bash
# MCP Smoke Tests for OnMind-XDB
# Usage: ./scripts/test-mcp.sh [base_url] [user:pass]
# Example: ./scripts/test-mcp.sh http://127.0.0.1:9990 admin:admin

set -euo pipefail

BASE="${1:-http://127.0.0.1:9990}"
AUTH="${2:-admin:admin}"

CURL="curl -s -u ${AUTH}"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

ok()  { echo -e "${GREEN}✓${NC} $*"; }
fail() { echo -e "${RED}✗${NC} $*"; exit 1; }
info() { echo -e "${YELLOW}→${NC} $*"; }

echo "=== OnMind-XDB MCP Smoke Tests ==="
echo "Base: $BASE"
echo "Auth: $AUTH"
echo

# 1. GET /mcp (info endpoint)
info "GET /mcp"
resp=$($CURL "$BASE/mcp")
echo "$resp" | jq -e '.ok == true and .service == "OnMind-XDB MCP"' >/dev/null \
  && ok "MCP info endpoint responds" \
  || fail "MCP info failed: $resp"

# 2. POST /mcp initialize
info "POST /mcp (initialize)"
resp=$($CURL -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":1,"method":"initialize"}' \
  "$BASE/mcp")
echo "$resp" | jq -e '.result.serverInfo.name == "onmind-xdb-mcp"' >/dev/null \
  && ok "initialize returns server info" \
  || fail "initialize failed: $resp"

# 3. POST /mcp tools/list
info "POST /mcp tools/list"
resp=$($CURL -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":2,"method":"tools/list"}' \
  "$BASE/mcp")
tool_count=$(echo "$resp" | jq '.result.tools | length')
echo "$resp" | jq -e '.result.tools | length > 0' >/dev/null \
  && ok "tools/list returns $tool_count tools" \
  || fail "tools/list failed: $resp"

# List available tool names
echo "$resp" | jq -r '.result.tools[].name' | sed 's/^/  - /'

# 4. POST /mcp tools/call abc_status
info "POST /mcp tools/call abc_status"
resp=$($CURL -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":3,"method":"tools/call","params":{"name":"abc_status","arguments":{}}}' \
  "$BASE/mcp")
echo "$resp" | jq -e '.result.isError == false' >/dev/null \
  && ok "abc_status works" \
  || fail "abc_status failed: $resp"

# 5. POST /mcp tools/call abc_list
info "POST /mcp tools/call abc_list"
resp=$($CURL -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":4,"method":"tools/call","params":{"name":"abc_list","arguments":{}}}' \
  "$BASE/mcp")
echo "$resp" | jq -e '.result.isError == false' >/dev/null \
  && ok "abc_list works" \
  || fail "abc_list failed: $resp"

# 6. GET /mcp/chat (chat info)
info "GET /mcp/chat"
resp=$($CURL "$BASE/mcp/chat")
echo "$resp" | jq -e '.ok == true and .service == "OnMind-XDB MCP Chat"' >/dev/null \
  && ok "Chat info endpoint responds" \
  || fail "Chat info failed: $resp"

# 7. POST /mcp/chat natural language: status
info "POST /mcp/chat 'status'"
resp=$($CURL -H 'Content-Type: application/json' \
  -d '{"message":"status"}' \
  "$BASE/mcp/chat")
echo "$resp" | jq -e '.ok == true and .tool.name == "abc_status"' >/dev/null \
  && ok "Chat 'status' routes to abc_status" \
  || fail "Chat status failed: $resp"

# 8. POST /mcp/chat natural language: list sheets
info "POST /mcp/chat 'list sheets'"
resp=$($CURL -H 'Content-Type: application/json' \
  -d '{"message":"list sheets"}' \
  "$BASE/mcp/chat")
echo "$resp" | jq -e '.ok == true and .tool.name == "abc_list"' >/dev/null \
  && ok "Chat 'list sheets' routes to abc_list" \
  || fail "Chat list failed: $resp"

# 9. POST /mcp/chat power-user /tool
info "POST /mcp/chat '/tool abc_find {...}'"
resp=$($CURL -H 'Content-Type: application/json' \
  -d '{"message":"/tool abc_find {\"some\":\"USER\",\"size\":\"2\"}"}' \
  "$BASE/mcp/chat")
echo "$resp" | jq -e '.tool.name == "abc_find"' >/dev/null \
  && ok "Chat /tool direct call works" \
  || fail "Chat /tool failed: $resp"

# 10. Verify write tools only appear when mcp.write=+
info "Checking write tools presence (expect: abc_create, abc_define, abc_schema if mcp.write=+)"
resp=$($CURL -H 'Content-Type: application/json' \
  -d '{"jsonrpc":"2.0","id":5,"method":"tools/list"}' \
  "$BASE/mcp")
write_tools=$(echo "$resp" | jq -r '.result.tools[] | select(.name | test("abc_(create|define|schema)")) | .name' | wc -l)
if [[ "$write_tools" -gt 0 ]]; then
  echo "$resp" | jq -r '.result.tools[] | select(.name | test("abc_(create|define|schema)")) | "  - \(.name) (write=\(.write))"'
  ok "Write tools present: $write_tools"
else
  echo "  (none — enable mcp.write=+ in onmind.ini to activate)"
fi

echo
echo "=== All MCP smoke tests passed ==="
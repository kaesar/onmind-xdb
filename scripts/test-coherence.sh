#!/bin/bash
# test-coherence.sh - Script para probar las nuevas funcionalidades de coherencia en OnMind-XDB

set -e

echo "=== OnMind-XDB Coherence Test ==="
echo

# Configuración
XDB_BASE_URL="http://localhost:9990"
AUTH_USER="admin"
AUTH_PASS="admin"

# Función para hacer requests con basic auth
make_request() {
    local method=$1
    local endpoint=$2
    local data=$3
    
    if [ -n "$data" ]; then
        curl -s -u "$AUTH_USER:$AUTH_PASS" -X "$method" -H "Content-Type: application/json" \
            "$XDB_BASE_URL$endpoint" -d "$data"
    else
        curl -s -u "$AUTH_USER:$AUTH_PASS" -X "$method" "$XDB_BASE_URL$endpoint"
    fi
}

# 1. Health Check
echo "1. Health Check..."
HEALTH=$(make_request GET "/abc")
echo "$HEALTH" | jq .
echo "✓ Health check passed"
echo

# 2. Test coherence endpoints
echo "2. Testing coherence endpoints..."

# Coherence stats
echo "2.1. Getting coherence statistics..."
COHERENCE=$(make_request GET "/api/store/coherence")
echo "$COHERENCE" | jq .

COHERENT=$(echo "$COHERENCE" | jq -r '.overall_coherent // false')
if [ "$COHERENT" = "true" ]; then
    echo "✓ Data is coherent"
else
    echo "⚠ Data coherence issues detected"
fi
echo

# Coherence verification
echo "2.2. Running coherence verification..."
VERIFY_RESULT=$(make_request POST "/api/store/coherence/verify")
echo "$VERIFY_RESULT" | jq .
echo "✓ Coherence verification completed"
echo

# Store health check
echo "2.3. Getting store health status..."
STORE_HEALTH=$(make_request GET "/api/store/health")
echo "$STORE_HEALTH" | jq .
echo "✓ Store health check completed"
echo

# Trace stats
echo "2.4. Getting trace statistics..."
TRACE_STATS=$(make_request GET "/api/trace/stats")
echo "$TRACE_STATS" | jq .
echo "✓ Trace statistics retrieved"
echo

# Test data creation to verify coherence is maintained
echo "3. Testing data creation and coherence..."
DATA_CREATION='{
    "what": "insert",
    "from": "xyany",
    "some": "TEST.COHERENCE",
    "data": {
        "anyxy": "TEST.COHERENCE",
        "any00": "test-coherence-data",
        "any01": "Test data for coherence verification",
        "any02": "automated-test"
    }
}'

echo "Creating test data via ABC API..."
DATA_RESULT=$(make_request POST "/abc" "$DATA_CREATION")
echo "$DATA_RESULT" | jq .
echo "✓ Test data created"

# Verify coherence after data creation
sleep 1
echo "3.1. Verifying coherence after data creation..."
COHERENCE_AFTER=$(make_request GET "/api/store/coherence")
COHERENT_AFTER=$(echo "$COHERENCE_AFTER" | jq -r '.overall_coherent // false')

if [ "$COHERENT_AFTER" = "true" ]; then
    echo "✓ Data coherence maintained after data creation"
else
    echo "⚠ Data coherence issues after data creation"
    echo "Attempting force sync..."
    SYNC_RESULT=$(make_request POST "/api/store/coherence/sync")
    echo "$SYNC_RESULT" | jq .
    
    # Verify after sync
    sleep 1
    COHERENCE_FINAL=$(make_request GET "/api/store/coherence")
    COHERENT_FINAL=$(echo "$COHERENCE_FINAL" | jq -r '.overall_coherent // false')
    
    if [ "$COHERENT_FINAL" = "true" ]; then
        echo "✓ Data coherence restored after force sync"
    else
        echo "⚠ Data coherence issues persist after force sync"
    fi
fi
echo

echo "=== Test Complete ==="
echo "Check ~/onmind/onmind-xdb.log for detailed logs (if logging is enabled)"
echo
echo "Available endpoints tested:"
echo "  • /abc - Main API endpoint"
echo "  • /api/store/coherence - Coherence statistics"
echo "  • /api/store/coherence/verify - Coherence verification"
echo "  • /api/store/coherence/sync - Force synchronization"
echo "  • /api/store/health - Store health check"
echo "  • /api/trace/stats - Trace logging statistics"
echo
echo "Configuration examples:"
echo "  • Maximum performance: app.logger=0, db.check=-"
echo "  • Basic monitoring: app.logger=1, db.check=-"
echo "  • Full debugging: app.logger=2, db.check=+"
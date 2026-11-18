const assert = require('assert');
const { calculateNextRevisionDate } = require('./srsEngine');

function runTests() {
    console.log("Running SRS Engine Tests...");
    
    // Test configuration
    const intervals = [1, 3, 7, 14, 30, 60];
    const baseDate = new Date('2026-07-18T12:00:00Z');
    
    // Test 1: Revision 0 (First Study) -> Next revision in 1 day
    const rev0Date = calculateNextRevisionDate(baseDate, intervals, 0);
    assert.strictEqual(rev0Date.toISOString(), new Date('2026-07-19T12:00:00Z').toISOString(), "Revision 0 failed");
    
    // Test 2: Revision 1 -> Next revision in 3 days
    const rev1Date = calculateNextRevisionDate(baseDate, intervals, 1);
    assert.strictEqual(rev1Date.toISOString(), new Date('2026-07-21T12:00:00Z').toISOString(), "Revision 1 failed");
    
    // Test 3: Revision 2 -> Next revision in 7 days
    const rev2Date = calculateNextRevisionDate(baseDate, intervals, 2);
    assert.strictEqual(rev2Date.toISOString(), new Date('2026-07-25T12:00:00Z').toISOString(), "Revision 2 failed");
    
    // Test 4: Revision 5 -> Next revision in 60 days
    const rev5Date = calculateNextRevisionDate(baseDate, intervals, 5);
    assert.strictEqual(rev5Date.toISOString(), new Date('2026-09-16T12:00:00Z').toISOString(), "Revision 5 failed");
    
    // Test 5: Revision 6 (Beyond defined intervals) -> Should return null
    const rev6Date = calculateNextRevisionDate(baseDate, intervals, 6);
    assert.strictEqual(rev6Date, null, "Revision 6 should return null");
    
    // Test 6: Empty intervals -> Should return null
    const emptyIntervalsDate = calculateNextRevisionDate(baseDate, [], 0);
    assert.strictEqual(emptyIntervalsDate, null, "Empty intervals should return null");
    
    // Test 7: Leap year check (Feb 28, 2024 -> +1 day -> Feb 29, 2024)
    const leapYearDate = new Date('2024-02-28T12:00:00Z');
    const leapYearRev = calculateNextRevisionDate(leapYearDate, intervals, 0);
    assert.strictEqual(leapYearRev.toISOString(), new Date('2024-02-29T12:00:00Z').toISOString(), "Leap year calculation failed");
    
    console.log("All tests passed successfully! ✅");
}

try {
    runTests();
} catch (error) {
    console.error("Test failed: ❌", error.message);
    process.exit(1);
}

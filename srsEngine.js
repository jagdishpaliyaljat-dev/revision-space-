/**
 * Calculates the next revision date based on user defined intervals and current revision count.
 * 
 * @param {Date} currentDate - The base date object to calculate from (usually today).
 * @param {number[]} intervals - An array of intervals in days (e.g., [1, 3, 7, 14, 30, 60]).
 * @param {number} currentRevisionCount - The number of revisions already completed (0 for newly studied).
 * @returns {Date|null} The next revision date, or null if all revisions are completed.
 */
function calculateNextRevisionDate(currentDate, intervals, currentRevisionCount) {
    if (!intervals || intervals.length === 0) {
        return null;
    }
    
    // If we've completed all intervals, return null (no more scheduled revisions)
    if (currentRevisionCount >= intervals.length) {
        return null;
    }
    
    const daysToAdd = intervals[currentRevisionCount];
    
    // Create a new date based on currentDate to avoid mutating the original
    const nextDate = new Date(currentDate.getTime());
    nextDate.setDate(nextDate.getDate() + daysToAdd);
    
    return nextDate;
}

module.exports = {
    calculateNextRevisionDate
};

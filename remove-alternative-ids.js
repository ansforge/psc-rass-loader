// Script to remove alternativeIds field from all PS documents in MongoDB
// This reverts the migration done by migrate-alternative-ids.js
// Usage: mongo <database> remove-alternative-ids.js

print("Starting removal of alternativeIds field from PS collection...");
print("Date: " + new Date());

// Get the PS collection
var db = db.getSiblingDB('pscload');
var psCollection = db.getCollection('ps');

// Count documents before operation
var totalDocs = psCollection.countDocuments({});
print("\nTotal PS documents in collection: " + totalDocs);

// Count documents that have alternativeIds field
var docsWithAlternativeIds = psCollection.countDocuments({ alternativeIds: { $exists: true } });
print("Documents with alternativeIds field: " + docsWithAlternativeIds);

if (docsWithAlternativeIds === 0) {
    print("\nNo documents have alternativeIds field. Nothing to do.");
} else {
    print("\n--- Starting removal operation ---");
    
    // Remove alternativeIds field from all documents
    var result = psCollection.updateMany(
        { alternativeIds: { $exists: true } },
        { $unset: { alternativeIds: "" } }
    );
    
    print("\n--- Removal operation completed ---");
    print("Matched documents: " + result.matchedCount);
    print("Modified documents: " + result.modifiedCount);
    
    // Verify removal
    var remainingDocs = psCollection.countDocuments({ alternativeIds: { $exists: true } });
    print("\nVerification - Documents still having alternativeIds: " + remainingDocs);
    
    if (remainingDocs === 0) {
        print("\n✓ SUCCESS: All alternativeIds fields have been removed!");
    } else {
        print("\n⚠ WARNING: " + remainingDocs + " documents still have alternativeIds field!");
    }
}

// Show sample document after operation
print("\n--- Sample document after removal ---");
var sampleDoc = psCollection.findOne({});
if (sampleDoc) {
    print("Document ID: " + sampleDoc._id);
    print("Has alternativeIds: " + (sampleDoc.alternativeIds !== undefined));
    if (sampleDoc.alternativeIds !== undefined) {
        print("alternativeIds value: " + JSON.stringify(sampleDoc.alternativeIds));
    }
}

print("\n--- Operation Summary ---");
print("Total documents: " + totalDocs);
print("Documents modified: " + result.modifiedCount);
print("Completion time: " + new Date());

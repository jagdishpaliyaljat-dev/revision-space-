sed -i 's/val list = snapshot?.toObjects(Topic::class.java) ?: emptyList()/val list = snapshot?.toObjects(Topic::class.java) ?: emptyList()/g' app/src/main/java/com/example/data/RevisionRepository.kt

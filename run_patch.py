import sys

with open('app/src/main/java/com/example/viewmodel/RevisionViewModel.kt', 'r') as f:
    content = f.read()

with open('to_replace.txt', 'r') as f:
    old_text = f.read()

with open('patch_vm_details.txt', 'r') as f:
    new_text = f.read()

content = content.replace(old_text, new_text)

with open('app/src/main/java/com/example/viewmodel/RevisionViewModel.kt', 'w') as f:
    f.write(content)

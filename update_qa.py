with open('app/src/main/java/com/example/viewmodel/RevisionViewModel.kt', 'r') as f:
    lines = f.readlines()

start_idx = -1
end_idx = -1

for i, line in enumerate(lines):
    if "fun injectQADummyData(" in line:
        start_idx = i
        break

if start_idx != -1:
    bracket_count = 0
    for i in range(start_idx, len(lines)):
        bracket_count += lines[i].count('{') - lines[i].count('}')
        if bracket_count == 0 and i > start_idx:
            end_idx = i
            break

if start_idx != -1 and end_idx != -1:
    new_func = """    fun injectQADummyData(onSuccess: () -> Unit, onFailure: (Exception) -> Unit) = launchSafe {
        _qaTestLog.value = emptyList()
        _qaTestProgress.value = 0f
        com.example.data.DummyDataGenerator.generate(
            viewModel = this@RevisionViewModel,
            onSuccess = onSuccess,
            onFailure = onFailure,
            logProgress = { msg, progress -> 
                _qaTestLog.value = _qaTestLog.value + listOf(msg)
                _qaTestStatus.value = msg
                _qaTestProgress.value = progress
            }
        )
    }\n"""
    
    lines = lines[:start_idx] + [new_func] + lines[end_idx+1:]
    
    with open('app/src/main/java/com/example/viewmodel/RevisionViewModel.kt', 'w') as f:
        f.writelines(lines)
    print("Replaced!")
else:
    print("Could not find function bounds.", start_idx, end_idx)

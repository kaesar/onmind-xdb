@param java.util.List<java.util.Map<String, Object>> settings

@template.layout(title = "Settings", content = @`
    <div class="p-8">
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-slate-800 ml-12">Settings</h1>
            <button onclick="goToNewSetting()" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 border-0 cursor-pointer">
                + New Setting
            </button>
        </div>
        
        @if(!settings.isEmpty())
        <div class="mb-4">
            <div class="relative">
                <i data-lucide="search" class="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400"></i>
                <input type="text" id="filterInput" onkeyup="filterTable(this, 'settingsTable')" placeholder="Filter settings..." class="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
        </div>
        @endif
        
        <div class="bg-white rounded-lg shadow">
            @if(settings.isEmpty())
                <div class="p-8 text-center text-slate-500">
                    <p class="text-lg">No settings found</p>
                    <p class="text-sm mt-2">Create your first setting to get started</p>
                </div>
            @else
                <div class="overflow-x-auto">
                    <table class="w-full">
                        <thead class="bg-slate-50 border-b">
                            <tr>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Code</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Name</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Value</th>
                                <th class="px-6 py-3 text-right text-xs font-medium text-slate-600 uppercase">Actions</th>
                            </tr>
                        </thead>
                        <tbody id="settingsTable" class="divide-y divide-slate-200">
                            @for(var setting : settings)
                                <tr class="hover:bg-slate-50">
                                    <td class="px-6 py-4 text-sm font-mono text-slate-800">${setting.get("set01").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-800">${setting.get("set02").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600 max-w-md truncate">${setting.get("set03").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-right">
                                        <div class="flex justify-end gap-2">
                                            <!-- TODO: Implement Edit functionality
                                            <button onclick="showToast('Function pending to include or release')" class="text-blue-600 hover:text-blue-800 p-1 border-0 bg-transparent cursor-pointer" title="Edit">
                                                <i data-lucide="pencil" class="w-4 h-4"></i>
                                            </button>
                                            -->
                                            <button onclick="deleteSetting('${setting.get("id").toString()}', '${setting.get("set02").toString()}')" class="text-red-600 hover:text-red-800 p-1 border-0 bg-transparent cursor-pointer" title="Delete">
                                                <i data-lucide="trash-2" class="w-4 h-4"></i>
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            @endfor
                        </tbody>
                    </table>
                </div>
            @endif
        </div>
    </div>
    
    <script>
        function goToNewSetting() {
            const query = {
                "what": "insert",
                "from": "xyset",
                "some": "CONFIG.SETUP",
                "puts": {
                    "set01": "CODE",
                    "set02": "name",
                    "set03": "value"
                }
            };
            sessionStorage.setItem('onmind-xdb-request', JSON.stringify(query, null, 2));
            sessionStorage.removeItem('onmind-xdb-response');
            sessionStorage.removeItem('onmind-xdb-response-time');
            window.location.href = '/_/';
        }
        
        async function deleteSetting(id, name) {
            if (!confirm('Delete setting "' + name + '"?')) return;
            
            const response = await fetch('/abc', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    what: 'delete',
                    from: 'xyset',
                    some: 'CONFIG.SETUP',
                    with: id
                })
            });
            
            const result = await response.json();
            
            if (result.ok || result.success) {
                showToast('Setting deleted successfully');
                setTimeout(() => window.location.reload(true), 800);
            } else {
                showToast(result.message || 'Error deleting setting');
            }
        }
    </script>
`)

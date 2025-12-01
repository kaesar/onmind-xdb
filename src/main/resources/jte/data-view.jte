@param java.util.Map<String, Object> sheet
@param java.util.List<java.util.Map<String, Object>> records
@param String code

@template.layout(title = sheet.get("kit02").toString(), content = @`
    <div class="p-8">
        <div class="mb-6 ml-12">
            <a href="/_/data" class="inline-flex items-center text-blue-600 hover:text-blue-800 p-2 rounded hover:bg-blue-50 no-underline cursor-pointer" title="Back to Collections">
                <i data-lucide="chevron-left" class="w-5 h-5"></i>
            </a>
        </div>
        
        <div class="flex justify-between items-center mb-6">
            <div class="ml-12">
                <h1 class="text-3xl font-bold text-slate-800">${sheet.get("kit03").toString()}</h1>
                <p class="text-sm text-slate-600 mt-1 font-mono">${code}</p>
            </div>
            <button onclick="goToNewRecord()" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 border-0 cursor-pointer">
                + New Record
            </button>
        </div>
        
        @if(!records.isEmpty())
        <div class="mb-4">
            <div class="relative">
                <i data-lucide="search" class="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400"></i>
                <input type="text" id="filterInput" onkeyup="filterTable(this, 'recordsTable')" placeholder="Filter records..." class="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
        </div>
        @endif
        
        <div class="bg-white rounded-lg shadow">
            @if(records.isEmpty())
                <div class="p-8 text-center text-slate-500">
                    <p class="text-lg">No records found</p>
                    <p class="text-sm mt-2">Add your first record to this collection</p>
                </div>
            @else
                <div class="overflow-x-auto">
                    <table class="w-full">
                        <thead class="bg-slate-50 border-b">
                            <tr>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">ID</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Code</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Data</th>
                                <th class="px-6 py-3 text-right text-xs font-medium text-slate-600 uppercase">Actions</th>
                            </tr>
                        </thead>
                        <tbody id="recordsTable" class="divide-y divide-slate-200">
                            @for(var record : records)
                                <tr class="hover:bg-slate-50">
                                    <td class="px-6 py-4 text-sm font-mono text-slate-600">${record.get("id").toString()}</td>
                                    <td class="px-6 py-4 text-sm font-mono text-slate-800">${record.get("any01").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">${record.get("any02").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-right">
                                        <div class="flex justify-end gap-2">
                                            <!--
                                            <button class="text-blue-600 hover:text-blue-800 p-1 border-0 bg-transparent cursor-pointer" title="Edit">
                                                <i data-lucide="pencil" class="w-4 h-4"></i>
                                            </button>
                                            TODO: Implement edit functionality
                                            -->
                                            <button onclick="deleteRecord('${record.get("id").toString()}')" class="text-red-600 hover:text-red-800 p-1 border-0 bg-transparent cursor-pointer" title="Delete">
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
    
    <script type="module">
        import * as abc from '/static/js/abcapi.js';
        
        window.goToNewRecord = function() {
            const query = {
                "what": "insert",
                "from": "xyany",
                "some": "${code}",
                "puts": {
                    "any01": "CODE",
                    "any02": "data"
                }
            };
            sessionStorage.setItem('onmind-xdb-request', JSON.stringify(query, null, 2));
            sessionStorage.removeItem('onmind-xdb-response');
            sessionStorage.removeItem('onmind-xdb-response-time');
            window.location.href = '/_/';
        }
        
        window.deleteRecord = async function(recordId) {
            if (!confirm('Are you sure you want to delete this record?')) return;
            
            const data = {
                from: 'xyany',
                some: '${code}',
                with: recordId
            };
            
            const result = await abc.remove('/abc', data);
            if (result.ok || result.success) {
                showToast('Record deleted successfully');
                setTimeout(() => location.reload(), 800);
            } else {
                showToast(result.message || 'Error deleting record');
            }
        };
    </script>
`)

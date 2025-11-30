@param java.util.List<java.util.Map<String, Object>> sheets

@template.layout(title = "Data", content = @`
    <div class="p-8">
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-slate-800 ml-12">Data Collections</h1>
            <button onclick="window.location.href='/_/sheets'" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 border-0 cursor-pointer">
                In Sheets...
            </button>
        </div>
        
        @if(!sheets.isEmpty())
        <div class="mb-4">
            <div class="relative">
                <i data-lucide="search" class="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400"></i>
                <input type="text" id="filterInput" onkeyup="filterTable(this, 'dataTable')" placeholder="Filter collections..." class="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
        </div>
        @endif
        
        <div class="bg-white rounded-lg shadow">
            @if(sheets.isEmpty())
                <div class="p-8 text-center text-slate-500">
                    <p class="text-lg">No data collections found</p>
                    <p class="text-sm mt-2">Create your first collection to get started</p>
                </div>
            @else
                <div class="overflow-x-auto">
                    <table class="w-full">
                        <thead class="bg-slate-50 border-b">
                            <tr>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Code</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Name</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Title</th>
                                <th class="px-6 py-3 text-right text-xs font-medium text-slate-600 uppercase">Actions</th>
                            </tr>
                        </thead>
                        <tbody id="dataTable" class="divide-y divide-slate-200">
                            @for(var sheet : sheets)
                                <tr class="hover:bg-slate-50">
                                    <td class="px-6 py-4 text-sm font-mono text-slate-800">${sheet.get("code").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-800">${sheet.get("name").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">${sheet.get("title").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-right">
                                        <div class="flex justify-end gap-2">
                                            <button onclick="window.location.href='/_/data/${sheet.get("name").toString()}'" class="text-blue-600 hover:text-blue-800 p-1 border-0 bg-transparent cursor-pointer" title="View">
                                                <i data-lucide="eye" class="w-4 h-4"></i>
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
`)

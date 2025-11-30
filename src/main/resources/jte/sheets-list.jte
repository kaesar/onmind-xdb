@param java.util.List<java.util.Map<String, Object>> sheets

@template.layout(title = "Sheets", content = @`
    <div class="p-8">
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-slate-800 ml-12">Sheet Definitions</h1>
            <button onclick="alert('Function pending to include or release')" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 border-0 cursor-pointer">
                + New Sheet
            </button>
        </div>
        
        <div class="bg-white rounded-lg shadow">
            @if(sheets.isEmpty())
                <div class="p-8 text-center text-slate-500">
                    <p class="text-lg">No sheets found</p>
                    <p class="text-sm mt-2">Create your first sheet definition to get started</p>
                </div>
            @else
                <div class="overflow-x-auto">
                    <table class="w-full">
                        <thead class="bg-slate-50 border-b">
                            <tr>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Code</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Name</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Title</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Scheme</th>
                                <th class="px-6 py-3 text-right text-xs font-medium text-slate-600 uppercase">Actions</th>
                            </tr>
                        </thead>
                        <tbody class="divide-y divide-slate-200">
                            @for(var sheet : sheets)
                                <tr class="hover:bg-slate-50">
                                    <td class="px-6 py-4 text-sm font-mono text-slate-800">${sheet.get("kit01").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-800">${sheet.get("kit02").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">${sheet.get("kit03").toString()}</td>
                                    <td class="px-6 py-4 text-sm">
                                        <span class="px-2 py-1 text-xs rounded bg-slate-100 text-slate-800 dark:bg-slate-700 dark:text-slate-200">
                                            ${sheet.get("kitxy").toString()}
                                        </span>
                                    </td>
                                    <td class="px-6 py-4 text-sm text-right">
                                        <div class="flex justify-end gap-2">
                                            <button class="text-blue-600 hover:text-blue-800 p-1 border-0 bg-transparent cursor-pointer" title="Edit">
                                                <i data-lucide="pencil" class="w-4 h-4"></i>
                                            </button>
                                            <button class="text-red-600 hover:text-red-800 p-1 border-0 bg-transparent cursor-pointer" title="Delete">
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
`)

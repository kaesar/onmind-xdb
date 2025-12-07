@param java.util.List<java.util.Map<String, Object>> sheets

@template.layout(title = "Sheets", content = @`
    <div class="p-8">
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-slate-800 ml-12">Sheet Definitions</h1>
            <button onclick="openNewSheetModal()" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 border-0 cursor-pointer">
                + New Sheet
            </button>
        </div>
        
        @if(!sheets.isEmpty())
        <div class="mb-4">
            <div class="relative">
                <i data-lucide="search" class="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400"></i>
                <input type="text" id="filterInput" onkeyup="filterTable(this, 'sheetsTable')" placeholder="Filter sheets..." class="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
        </div>
        @endif
        
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
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Model</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Scheme</th>
                                <th class="px-6 py-3 text-right text-xs font-medium text-slate-600 uppercase">Actions</th>
                            </tr>
                        </thead>
                        <tbody id="sheetsTable" class="divide-y divide-slate-200">
                            @for(var sheet : sheets)
                                <tr class="hover:bg-slate-50">
                                    <td class="px-6 py-4 text-sm font-mono text-slate-800">${sheet.get("kit01").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-800">${sheet.get("kit02").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">${sheet.get("kit03").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-600">${sheet.get("kit05").toString()}</td>
                                    <td class="px-6 py-4 text-sm">
                                        <span class="px-2 py-1 text-xs rounded ${sheet.get("kitxy").toString().equals("SETUP") ? "bg-purple-100 text-purple-800" : "bg-blue-100 text-blue-800"}">
                                            ${sheet.get("kitxy").toString()}
                                        </span>
                                    </td>
                                    <td class="px-6 py-4 text-sm text-right">
                                        <div class="flex justify-end gap-2">
                                            <button onclick="editSheet('${sheet.get("kit01").toString()}', '${sheet.get("kit02").toString()}', '${sheet.get("kit03").toString()}', '${sheet.get("kitxy").toString()}', '${sheet.get("kit05").toString()}')" class="text-blue-600 hover:text-blue-800 p-1 border-0 bg-transparent cursor-pointer" title="Edit">
                                                <i data-lucide="pencil" class="w-4 h-4"></i>
                                            </button>
                                            <button onclick="deleteSheet('${sheet.get("kit01").toString()}')" class="text-red-600 hover:text-red-800 p-1 border-0 bg-transparent cursor-pointer" title="Delete">
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
    
    <!-- Modal -->
    <div id="newSheetModal" class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
            <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-bold text-slate-800">New Sheet</h2>
                <button onclick="closeNewSheetModal()" class="text-slate-400 hover:text-slate-600 border-0 bg-transparent cursor-pointer">
                    <i data-lucide="x" class="w-5 h-5"></i>
                </button>
            </div>
            
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Name</label>
                    <input type="text" id="sheetName" placeholder="example" class="w-full px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Title</label>
                    <input type="text" id="sheetTitle" placeholder="Example Sheet" class="w-full px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Type</label>
                    <select id="sheetType" class="w-full px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <option value="SHEET">SHEET</option>
                        <option value="SETUP">SETUP</option>
                    </select>
                </div>
            </div>
            
            <div class="flex justify-end gap-2 mt-6">
                <button onclick="closeNewSheetModal()" class="px-4 py-2 border border-slate-300 rounded hover:bg-slate-50 cursor-pointer">
                    Cancel
                </button>
                <button onclick="createSheet()" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 border-0 cursor-pointer">
                    Create
                </button>
            </div>
        </div>
    </div>
    
    <!-- Delete Confirmation Modal -->
    <div id="deleteConfirmModal" class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-sm">
            <div class="flex items-center mb-4">
                <div class="flex-shrink-0 w-10 h-10 mx-auto bg-red-100 rounded-full flex items-center justify-center">
                    <i data-lucide="alert-triangle" class="w-6 h-6 text-red-600"></i>
                </div>
            </div>
            
            <div class="text-center">
                <h3 class="text-lg font-medium text-slate-800 mb-2">Delete Sheet</h3>
                <p class="text-sm text-slate-500 mb-6" id="deleteMessage">Are you sure you want to delete this sheet? This action cannot be undone.</p>
            </div>
            
            <div class="flex gap-3">
                <button onclick="closeDeleteModal()" class="flex-1 px-4 py-2 border border-slate-300 rounded hover:bg-slate-50 cursor-pointer">
                    Cancel
                </button>
                <button onclick="confirmDelete()" class="flex-1 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 border-0 cursor-pointer">
                    Delete
                </button>
            </div>
        </div>
    </div>
    
    <!-- Edit Sheet Modal -->
    <div id="editSheetModal" class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
            <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-bold text-slate-800">Edit Sheet</h2>
                <button onclick="closeEditSheetModal()" class="text-slate-400 hover:text-slate-600 border-0 bg-transparent cursor-pointer">
                    <i data-lucide="x" class="w-5 h-5"></i>
                </button>
            </div>
            
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Code</label>
                    <input type="text" id="editSheetCode" readonly class="w-full px-3 py-2 border border-slate-300 rounded bg-slate-50 text-slate-600 cursor-not-allowed">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Name</label>
                    <input type="text" id="editSheetName" readonly class="w-full px-3 py-2 border border-slate-300 rounded bg-slate-50 text-slate-600 cursor-not-allowed">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Title</label>
                    <input type="text" id="editSheetTitle" class="w-full px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Type</label>
                    <input type="text" id="editSheetType" readonly class="w-full px-3 py-2 border border-slate-300 rounded bg-slate-50 text-slate-600 cursor-not-allowed">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Model/Spec</label>
                    <textarea id="editSheetSpec" rows="3" placeholder="[]" class="w-full px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500"></textarea>
                </div>
            </div>
            
            <div class="flex justify-end gap-2 mt-6">
                <button onclick="closeEditSheetModal()" class="px-4 py-2 border border-slate-300 rounded hover:bg-slate-50 cursor-pointer">
                    Cancel
                </button>
                <button onclick="updateSheet()" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 border-0 cursor-pointer">
                    Update
                </button>
            </div>
        </div>
    </div>
    
    <script type="module">
        import * as abc from '/static/js/abcapi.js';
        
        window.openNewSheetModal = function() {
            document.getElementById('newSheetModal').classList.remove('hidden');
            lucide.createIcons();
        }
        
        window.closeNewSheetModal = function() {
            document.getElementById('newSheetModal').classList.add('hidden');
            document.getElementById('sheetName').value = '';
            document.getElementById('sheetTitle').value = '';
            document.getElementById('sheetType').value = 'SHEET';
        }
        
        window.createSheet = async function() {
            const name = document.getElementById('sheetName').value.trim();
            const title = document.getElementById('sheetTitle').value.trim();
            const type = document.getElementById('sheetType').value;
            
            if (!name) {
                showToast('Name is required');
                return;
            }
            
            const whoamiResult = await abc.whoami('/abc');
            const user = whoamiResult.data?.user || 'admin';
            
            const data = {
                what: 'create',
                from: 'xyany',
                some: name,
                with: type,
                show: title || name,
                user: user
            };
            
            const result = await abc.create('/abc', data);
            
            if (result.ok) {
                showToast('Sheet created successfully');
                closeNewSheetModal();
                setTimeout(() => window.location.reload(true), 800);
            } else {
                showToast(result.message || 'Error creating sheet');
            }
        }
        
        let deleteSheetData = null;
        
        window.deleteSheet = function(code) {
            deleteSheetData = { code };
            document.getElementById('deleteMessage').textContent = 'Are you sure you want to delete sheet "' + code + '"? This action cannot be undone.';
            document.getElementById('deleteConfirmModal').classList.remove('hidden');
            lucide.createIcons();
        }
        
        window.closeDeleteModal = function() {
            document.getElementById('deleteConfirmModal').classList.add('hidden');
            deleteSheetData = null;
        }
        
        window.confirmDelete = async function() {
            if (!deleteSheetData) return;
            
            const whoamiResult = await abc.whoami('/abc');
            const user = whoamiResult.data?.user || 'admin';
            
            const parts = deleteSheetData.code.split('.');
            const name = parts[0];
            const type = parts[1] || 'SHEET';
            
            const data = {
                what: 'drop',
                from: 'xyany',
                some: name,
                with: type,
                user: user
            };
            
            const result = await abc.drop('/abc', data);
            
            if (result.ok || result.success) {
                showToast('Sheet deleted successfully');
                closeDeleteModal();
                setTimeout(() => window.location.reload(true), 800);
            } else {
                showToast(result.message || 'Error deleting sheet');
            }
        }
        
        let editSheetData = null;
        
        window.editSheet = function(code, name, title, type, spec) {
            editSheetData = { code, name, title, type, spec };
            document.getElementById('editSheetCode').value = code;
            document.getElementById('editSheetName').value = name;
            document.getElementById('editSheetTitle').value = title;
            document.getElementById('editSheetType').value = type;
            document.getElementById('editSheetSpec').value = spec || '[]';
            document.getElementById('editSheetModal').classList.remove('hidden');
            lucide.createIcons();
        }
        
        window.closeEditSheetModal = function() {
            document.getElementById('editSheetModal').classList.add('hidden');
            editSheetData = null;
        }
        
        window.updateSheet = async function() {
            if (!editSheetData) return;
            
            const name = document.getElementById('editSheetName').value.trim();
            const title = document.getElementById('editSheetTitle').value.trim();
            const type = document.getElementById('editSheetType').value;
            const spec = document.getElementById('editSheetSpec').value.trim();
            
            if (!name) {
                showToast('Name is required');
                return;
            }
            
            const whoamiResult = await abc.whoami('/abc');
            const user = whoamiResult.data?.user || 'admin';
            
            const data = {
                what: 'define',
                some: name,
                with: type,
                puts: spec || '[]',
                show: title,
                user: user
            };
            
            const result = await abc.define('/abc', data);
            
            if (result.ok) {
                showToast('Sheet updated successfully');
                closeEditSheetModal();
                setTimeout(() => window.location.reload(true), 800);
            } else {
                showToast(result.message || 'Error updating sheet');
            }
        }
    </script>
`)

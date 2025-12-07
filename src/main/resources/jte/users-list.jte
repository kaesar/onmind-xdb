@param java.util.List<java.util.Map<String, Object>> users

@template.layout(title = "Users", content = @`
    <div class="p-8">
        <div class="flex justify-between items-center mb-6">
            <h1 class="text-3xl font-bold text-slate-800 ml-12">Users & Roles</h1>
            <button onclick="openNewUserModal()" class="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 border-0 cursor-pointer">
                + New User
            </button>
        </div>
        
        @if(!users.isEmpty())
        <div class="mb-4">
            <div class="relative">
                <i data-lucide="search" class="w-5 h-5 absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400"></i>
                <input type="text" id="filterInput" onkeyup="filterTable(this, 'usersTable')" placeholder="Filter users..." class="w-full pl-10 pr-4 py-2 border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            </div>
        </div>
        @endif
        
        <div class="bg-white rounded-lg shadow">
            @if(users.isEmpty())
                <div class="p-8 text-center text-slate-500">
                    <p class="text-lg">No users found</p>
                    <p class="text-sm mt-2">Create your first user to get started</p>
                </div>
            @else
                <div class="overflow-x-auto">
                    <table class="w-full">
                        <thead class="bg-slate-50 border-b">
                            <tr>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Code</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Name</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Type</th>
                                <th class="px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase">Status</th>
                                <th class="px-6 py-3 text-right text-xs font-medium text-slate-600 uppercase">Actions</th>
                            </tr>
                        </thead>
                        <tbody id="usersTable" class="divide-y divide-slate-200">
                            @for(var user : users)
                                <tr class="hover:bg-slate-50">
                                    <td class="px-6 py-4 text-sm font-mono text-slate-800">${user.get("key01").toString()}</td>
                                    <td class="px-6 py-4 text-sm text-slate-800">${user.get("key02").toString()}</td>
                                    <td class="px-6 py-4 text-sm">
                                        <span class="px-2 py-1 text-xs rounded ${user.get("keyxy").toString().equals("USER") ? "bg-blue-100 text-blue-800" : "bg-purple-100 text-purple-800"}">
                                            ${user.get("keyxy").toString()}
                                        </span>
                                    </td>
                                    <td class="px-6 py-4 text-sm">
                                        <span class="px-2 py-1 text-xs rounded ${user.get("key20").toString().equals("OK") ? "bg-green-100 text-green-800" : "bg-red-100 text-red-800"}">
                                            ${user.get("key20").toString()}
                                        </span>
                                    </td>
                                    <td class="px-6 py-4 text-sm text-right">
                                        <div class="flex justify-end gap-2">
                                            <!-- TODO: Implement Edit functionality
                                            <button onclick="showToast('Function pending to include or release')" class="text-blue-600 hover:text-blue-800 p-1 border-0 bg-transparent cursor-pointer" title="Edit">
                                                <i data-lucide="pencil" class="w-4 h-4"></i>
                                            </button>
                                            -->
                                            <button onclick="deleteUser('${user.get("id").toString()}', '${user.get("key02").toString()}')" class="text-red-600 hover:text-red-800 p-1 border-0 bg-transparent cursor-pointer" title="Delete">
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
    
    <!-- New User Modal -->
    <div id="newUserModal" class="hidden fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div class="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
            <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-bold text-slate-800">New User</h2>
                <button onclick="closeNewUserModal()" class="text-slate-400 hover:text-slate-600 border-0 bg-transparent cursor-pointer">
                    <i data-lucide="x" class="w-5 h-5"></i>
                </button>
            </div>
            
            <div class="space-y-4">
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Name</label>
                    <input type="text" id="userName" placeholder="john" class="w-full px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Email</label>
                    <input type="email" id="userEmail" placeholder="john@example.com" class="w-full px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500">
                </div>
                <div>
                    <label class="block text-sm font-medium text-slate-700 mb-1">Type</label>
                    <select id="userType" class="w-full px-3 py-2 border border-slate-300 rounded focus:outline-none focus:ring-2 focus:ring-blue-500">
                        <option value="USER">USER</option>
                        <option value="ROLE">ROLE</option>
                    </select>
                </div>
            </div>
            
            <div class="flex justify-end gap-2 mt-6">
                <button onclick="closeNewUserModal()" class="px-4 py-2 border border-slate-300 rounded hover:bg-slate-50 cursor-pointer">
                    Cancel
                </button>
                <button onclick="createUser()" class="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 border-0 cursor-pointer">
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
                <h3 class="text-lg font-medium text-slate-800 mb-2">Delete User</h3>
                <p class="text-sm text-slate-500 mb-6" id="deleteMessage">Are you sure you want to delete this user? This action cannot be undone.</p>
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
    
    <script type="module">
        import * as abc from '/static/js/abcapi.js';
        
        window.openNewUserModal = function() {
            document.getElementById('newUserModal').classList.remove('hidden');
            lucide.createIcons();
        }
        
        window.closeNewUserModal = function() {
            document.getElementById('newUserModal').classList.add('hidden');
            document.getElementById('userName').value = '';
            document.getElementById('userEmail').value = '';
            document.getElementById('userType').value = 'USER';
        }
        
        window.createUser = async function() {
            const name = document.getElementById('userName').value.trim();
            const email = document.getElementById('userEmail').value.trim();
            const scheme = document.getElementById('userType').value;
            
            if (!name) {
                showToast('Name is required');
                return;
            }
            if (!email) {
                showToast('Email is required');
                return;
            }
            
            const data = {
                some: name,
                user: email,
                with: scheme
            };
            
            const result = await abc.signup('/abc', data);
            
            if (result.ok) {
                showToast('User created successfully');
                closeNewUserModal();
                setTimeout(() => window.location.reload(true), 800);
            } else {
                showToast(result.message || 'Error creating user');
            }
        }
        
        let deleteUserData = null;
        
        window.deleteUser = function(id, name) {
            deleteUserData = { id, name };
            document.getElementById('deleteMessage').textContent = 'Are you sure you want to delete user "' + name + '"? This action cannot be undone.';
            document.getElementById('deleteConfirmModal').classList.remove('hidden');
            lucide.createIcons();
        }
        
        window.closeDeleteModal = function() {
            document.getElementById('deleteConfirmModal').classList.add('hidden');
            deleteUserData = null;
        }
        
        window.confirmDelete = async function() {
            if (!deleteUserData) return;
            
            const data = {
                from: 'xykey',
                some: 'USER',
                with: deleteUserData.id
            };
            
            const result = await abc.remove('/abc', data);
            
            if (result.ok || result.success) {
                showToast('User deleted successfully');
                closeDeleteModal();
                setTimeout(() => window.location.reload(true), 800);
            } else {
                showToast(result.message || 'Error deleting user');
            }
        }
    </script>
`)

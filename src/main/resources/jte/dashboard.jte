@param String title

@template.layout(title = title, content = @`
    <div class="p-8">
        <h1 class="text-3xl font-bold text-slate-800 mb-6 ml-12">eXpress DataBase WebApp</h1>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            <a href="/_/data" class="bg-white rounded-lg shadow p-6 hover:shadow-lg dark:hover:shadow-slate-700/50 transition cursor-pointer no-underline">
                <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <i data-lucide="database" class="w-10 h-10 text-blue-500"></i>
                        <div>
                            <p class="text-sm text-slate-600">Data Collections</p>
                        </div>
                    </div>
                    <i data-lucide="chevron-right" class="w-6 h-6 text-slate-400"></i>
                </div>
            </a>
            
            <a href="/_/users" class="bg-white rounded-lg shadow p-6 hover:shadow-lg dark:hover:shadow-slate-700/50 transition cursor-pointer no-underline">
                <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <i data-lucide="users" class="w-10 h-10 text-purple-500"></i>
                        <div>
                            <p class="text-sm text-slate-600">Users</p>
                        </div>
                    </div>
                    <i data-lucide="chevron-right" class="w-6 h-6 text-slate-400"></i>
                </div>
            </a>
            
            <a href="/_/settings" class="bg-white rounded-lg shadow p-6 hover:shadow-lg dark:hover:shadow-slate-700/50 transition cursor-pointer no-underline">
                <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <i data-lucide="settings" class="w-10 h-10 text-orange-500"></i>
                        <div>
                            <p class="text-sm text-slate-600">Settings</p>
                        </div>
                    </div>
                    <i data-lucide="chevron-right" class="w-6 h-6 text-slate-400"></i>
                </div>
            </a>
            
            <a href="/_/sheets" class="bg-white rounded-lg shadow p-6 hover:shadow-lg dark:hover:shadow-slate-700/50 transition cursor-pointer no-underline">
                <div class="flex items-center justify-between">
                    <div class="flex items-center gap-3">
                        <i data-lucide="table" class="w-10 h-10 text-green-500"></i>
                        <div>
                            <p class="text-sm text-slate-600">Sheets</p>
                        </div>
                    </div>
                    <i data-lucide="chevron-right" class="w-6 h-6 text-slate-400"></i>
                </div>
            </a>
        </div>
        
        <div class="bg-white rounded-lg shadow p-6">
            <div class="flex justify-between items-center mb-4">
                <div class="flex items-center gap-2">
                    <h2 class="text-xl font-semibold text-slate-800">Query</h2>
                    <i data-lucide="chevron-right" class="w-5 h-5 text-slate-400"></i>
                    <span class="text-lg text-slate-600">/abc</span>
                </div>
                <button onclick="executeRequest()" class="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 flex items-center gap-2 border-0 cursor-pointer">
                    <i data-lucide="play" class="w-4 h-4"></i>
                    Execute
                </button>
            </div>
            
            <div class="mb-4">
                <label class="block text-sm font-medium text-slate-700 mb-2">Request Body (JSON)</label>
                <div id="editor" style="height: 180px; border: 1px solid #e2e8f0; border-radius: 0.375rem;"></div>
            </div>
            
            <div>
                <div class="flex items-center gap-2 mb-2">
                    <label class="text-sm font-medium text-slate-700">Response</label>
                    <i data-lucide="chevron-right" class="w-4 h-4 text-slate-400"></i>
                    <span id="responseTime" class="text-sm text-slate-500"></span>
                </div>
                <div id="response" style="height: 350px; border: 1px solid #e2e8f0; border-radius: 0.375rem;"></div>
                <div id="responseTable" class="hidden overflow-auto bg-white" style="max-height: 350px; border: 1px solid #e2e8f0; border-radius: 0.375rem;">
                    <table class="w-full">
                        <thead class="bg-slate-50 border-b sticky top-0">
                            <tr id="tableHeader"></tr>
                        </thead>
                        <tbody id="tableBody" class="divide-y divide-slate-200"></tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
    
    <script src="/static/js/ace.js"></script>
    <script type="module">
        import * as abc from '/static/js/abcapi.js';
        ace.config.set('basePath', '/static/js');
        const editor = ace.edit("editor");
        editor.setTheme("ace/theme/github");
        editor.session.setMode("ace/mode/json");
        
        const savedRequest = sessionStorage.getItem('onmind-xdb-request');
        const defaultRequest = {
            "what": "find",
            "from": "xyany",
            "some": "PRODUCTS.SHEET",
            "size": "100"
        };
        editor.setValue(JSON.stringify(savedRequest ? JSON.parse(savedRequest) : defaultRequest, null, 2), -1);
        
        const responseEditor = ace.edit("response");
        responseEditor.setTheme("ace/theme/github");
        responseEditor.session.setMode("ace/mode/json");
        responseEditor.setReadOnly(true);
        
        const savedResponse = sessionStorage.getItem('onmind-xdb-response');
        const savedTime = sessionStorage.getItem('onmind-xdb-response-time');
        const savedViewMode = localStorage.getItem('onmind-xdb-view-mode');
        
        if (savedResponse) {
            const responseData = JSON.parse(savedResponse);
            if (savedTime) document.getElementById('responseTime').textContent = savedTime;
            if (savedViewMode === 'table' && responseData.total > 0 && responseData.data) {
                showTableView(responseData.data, JSON.parse(savedRequest || '{}').from);
            } else {
                showJsonView(responseData);
            }
        }
        
        window.executeRequest = async function() {
            try {
                const bodyText = editor.getValue();
                const bodyObj = JSON.parse(bodyText);
                
                const startTime = performance.now();
                const data = await abc.ask('/abc', bodyObj);
                const endTime = performance.now();
                const duration = ((endTime - startTime) / 1000).toFixed(3);
                
                document.getElementById('responseTime').textContent = duration + 's';

                if (data.total > 0 && data.data) {
                    localStorage.setItem('onmind-xdb-view-mode', 'table');
                    showTableView(data.data, bodyObj.from);
                } else {
                    localStorage.setItem('onmind-xdb-view-mode', 'json');
                    showJsonView(data);
                }

                sessionStorage.setItem('onmind-xdb-request', bodyText);
                sessionStorage.setItem('onmind-xdb-response', JSON.stringify(data));
                sessionStorage.setItem('onmind-xdb-response-time', duration + 's');
            } catch (error) {
                document.getElementById('responseTime').textContent = '';
                showJsonView({ error: error.message });
            }
        }
        
        function showJsonView(data) {
            document.getElementById('response').classList.remove('hidden');
            document.getElementById('responseTable').classList.add('hidden');
            responseEditor.setValue(JSON.stringify(data, null, 2), -1);
        }
        
        function showTableView(records, fromTable) {
            document.getElementById('response').classList.add('hidden');
            document.getElementById('responseTable').classList.remove('hidden');
            
            const header = document.getElementById('tableHeader');
            const body = document.getElementById('tableBody');
            header.innerHTML = '';
            body.innerHTML = '';
            
            if (!records || records.length === 0) return;
            
            const columns = Object.keys(records[0]);
            columns.forEach(col => {
                const th = document.createElement('th');
                th.className = 'px-6 py-3 text-left text-xs font-medium text-slate-600 uppercase';
                th.textContent = col;
                header.appendChild(th);
            });
            
            records.forEach(record => {
                const tr = document.createElement('tr');
                tr.className = 'hover:bg-slate-50';
                columns.forEach(col => {
                    const td = document.createElement('td');
                    td.className = 'px-6 py-4 text-sm text-slate-800';
                    const value = record[col];
                    td.textContent = value !== null && value !== undefined ? value : '';
                    tr.appendChild(td);
                });
                body.appendChild(tr);
            });
        }
        
        lucide.createIcons();
    </script>
`)

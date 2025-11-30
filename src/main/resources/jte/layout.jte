@param String title = "OnMind-XDB"
@param gg.jte.Content content

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title} - OnMind-XDB</title>
    <script src="/static/js/tailwind.min.js"></script>
    <script>
        tailwind.config = {
            corePlugins: {
                preflight: false,
            }
        }
    </script>
    <script src="/static/js/lucide.min.js"></script>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body { font-family: system-ui, -apple-system, sans-serif; overflow-x: hidden; transition: background-color 0.3s, color 0.3s; }
        .sidebar { width: 250px; background: #1e293b; color: white; min-height: 100vh; position: fixed; left: 0; top: 0; transition: transform 0.3s, background-color 0.3s; z-index: 1000; }
        .sidebar.collapsed { transform: translateX(-250px); }
        .sidebar a { display: flex; align-items: center; gap: 0.75rem; padding: 0.75rem 1.5rem; color: #cbd5e1; text-decoration: none; transition: all 0.2s; }
        .sidebar a:hover, .sidebar a.active { background: #334155; color: white; }
        .main-content { background: #f8fafc; margin-left: 250px; transition: margin-left 0.3s, background-color 0.3s; min-height: 100vh; }
        .main-content.expanded { margin-left: 0; }
        .toggle-btn { position: fixed; top: 1.5rem; left: 270px; z-index: 1001; background: white; border: 1px solid #e2e8f0; border-radius: 0.375rem; padding: 0.5rem; cursor: pointer; transition: all 0.3s; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
        .toggle-btn.collapsed { left: 20px; }
        .theme-btn { background: transparent; border: none; padding: 0.5rem; cursor: pointer; transition: all 0.3s; }
        .theme-btn:hover { background: #334155; border-radius: 0.375rem; }
        /* Dark theme */
        body.dark { background: #0f172a; color: #e2e8f0; }
        body.dark .sidebar { background: #020617; }
        body.dark .main-content { background: #0f172a; }
        body.dark .toggle-btn { background: #1e293b; border-color: #334155; }
        body.dark .toggle-btn i { color: #e2e8f0; }
        .theme-btn i { color: #ffffff !important; }
        body.dark .theme-btn i { color: #ffffff !important; }
        tbody td { padding-top: 0.25rem !important; padding-bottom: 0.25rem !important; }
        body.dark .text-slate-800 { color: #f1f5f9; }
        body.dark .text-slate-700 { color: #e2e8f0; }
        body.dark .text-slate-600 { color: #cbd5e1; }
        body.dark .bg-white { background: #1e293b; }
        body.dark .border-slate-200 { border-color: #334155; }
        body.dark .bg-slate-50 { background: #0f172a; }
        body.dark tr:hover td { color: #1f2937 !important; }
        tbody button, tbody a { cursor: pointer; }
        tbody button i, tbody a i { pointer-events: none; }
        /* Toast notifications */
        .toast { position: fixed; bottom: 20px; right: 20px; background: #1e293b; color: white; padding: 1rem 1.5rem; border-radius: 0.5rem; box-shadow: 0 4px 6px rgba(0,0,0,0.1); z-index: 9999; animation: slideIn 0.3s ease-out; display: flex; align-items: center; gap: 0.75rem; min-width: 300px; }
        .toast.hide { animation: slideOut 0.3s ease-out; }
        @keyframes slideIn { from { transform: translateX(400px); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
        @keyframes slideOut { from { transform: translateX(0); opacity: 1; } to { transform: translateX(400px); opacity: 0; } }
        body.dark .toast { background: #334155; }
    </style>
</head>
<body>
    <button onclick="toggleSidebar()" class="toggle-btn" id="toggleBtn">
        <i data-lucide="menu" class="w-5 h-5 text-slate-700"></i>
    </button>
    <nav class="sidebar" id="sidebar">
        <div class="p-6 border-b border-slate-700 flex justify-between items-center">
            <div>
                <h1 class="text-xl font-bold">OnMind-XDB</h1>
                <p class="text-xs text-slate-400 mt-1">Admin Panel</p>
            </div>
            <button onclick="toggleTheme()" class="theme-btn" id="themeBtn">
                <i data-lucide="sun" class="w-5 h-5" id="themeIcon" style="color: #ffffff !important; stroke: #ffffff !important;"></i>
            </button>
        </div>
        <div class="py-4">
            <a href="/_/">
                <i data-lucide="layout-dashboard" class="w-5 h-5"></i>
                <span>Dashboard</span>
            </a>
            <a href="/_/data">
                <i data-lucide="database" class="w-5 h-5"></i>
                <span>Data</span>
            </a>
            <a href="/_/users">
                <i data-lucide="users" class="w-5 h-5"></i>
                <span>Users</span>
            </a>
            <a href="/_/settings">
                <i data-lucide="settings" class="w-5 h-5"></i>
                <span>Settings</span>
            </a>
            <a href="/_/sheets">
                <i data-lucide="table" class="w-5 h-5"></i>
                <span>Sheets</span>
            </a>
        </div>
    </nav>
    <main class="main-content" id="mainContent">
        ${content}
    </main>
    <script>
        // Theme management
        function initTheme() {
            const stored = localStorage.getItem('onmind-xdb-theme');
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
            const theme = stored || (prefersDark ? 'dark' : 'light');
            applyTheme(theme);
        }
        
        function applyTheme(theme) {
            document.body.classList.toggle('dark', theme === 'dark');
            const icon = document.getElementById('themeIcon');
            icon.setAttribute('data-lucide', theme === 'dark' ? 'sun' : 'moon');
            lucide.createIcons();
        }
        
        function toggleTheme() {
            const isDark = document.body.classList.contains('dark');
            const newTheme = isDark ? 'light' : 'dark';
            localStorage.setItem('onmind-xdb-theme', newTheme);
            applyTheme(newTheme);
        }
        
        function toggleSidebar() {
            document.getElementById('sidebar').classList.toggle('collapsed');
            document.getElementById('mainContent').classList.toggle('expanded');
            document.getElementById('toggleBtn').classList.toggle('collapsed');
        }
        
        function initMobile() {
            if (window.innerWidth < 768) {
                document.getElementById('sidebar').classList.add('collapsed');
                document.getElementById('mainContent').classList.add('expanded');
                document.getElementById('toggleBtn').classList.add('collapsed');
            }
        }
        
        initTheme();
        initMobile();
        lucide.createIcons();
        
        // Table filter function
        function filterTable(input, tableId) {
            const filter = input.value.toLowerCase();
            const table = document.getElementById(tableId);
            const rows = table.getElementsByTagName('tr');
            
            for (let i = 0; i < rows.length; i++) {
                const cells = rows[i].getElementsByTagName('td');
                let found = false;
                
                for (let j = 0; j < cells.length; j++) {
                    const cell = cells[j];
                    if (cell && cell.textContent.toLowerCase().indexOf(filter) > -1) {
                        found = true;
                        break;
                    }
                }
                
                rows[i].style.display = found || cells.length === 0 ? '' : 'none';
            }
        }
        
        // Toast notification
        function showToast(message) {
            const toast = document.createElement('div');
            toast.className = 'toast';
            toast.innerHTML = '<i data-lucide="info" class="w-5 h-5"></i><span>' + message + '</span>';
            document.body.appendChild(toast);
            lucide.createIcons();
            
            setTimeout(function() {
                toast.classList.add('hide');
                setTimeout(function() { toast.remove(); }, 300);
            }, 3000);
        }
    </script>
</body>
</html>


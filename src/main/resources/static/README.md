# Static Assets - UI Libraries

This directory contains the UI libraries required for the frontend to work offline without internet connection.

## Included Libraries

### JavaScript (`/js`)

- **abcapi.js** (2021-06-28)
  - My own JS client written as static resource for OnMind-XDB with `/abc` endpoint

- **tailwind.min.js** (v3.4.1)
  - Utility-first CSS framework
  - Source: https://cdn.tailwindcss.com/3.4.1
  - License: MIT

- **lucide.min.js** (v0.454.0)
  - Icon library
  - Source: https://unpkg.com/lucide@0.454.0/dist/umd/lucide.min.js
  - License: ISC

- **ace.js** (v1.32.2)
  - Code editor for dashboard
  - Source: https://cdnjs.cloudflare.com/ajax/libs/ace/1.32.2/ace.js
  - License: BSD 3-Clause

- **mode-json.js** (v1.32.2)
  - JSON mode for Ace Editor
  - Source: https://cdnjs.cloudflare.com/ajax/libs/ace/1.32.2/mode-json.js
  - License: BSD 3-Clause

- **theme-github.js** (v1.32.2)
  - GitHub theme for Ace Editor
  - Source: https://cdnjs.cloudflare.com/ajax/libs/ace/1.32.2/theme-github.js
  - License: BSD 3-Clause

- **worker-json.js** (v1.32.2)
  - JSON validation worker for Ace Editor
  - Source: https://cdnjs.cloudflare.com/ajax/libs/ace/1.32.2/worker-json.js
  - License: BSD 3-Clause

### CSS (`/css`)

Currently empty - Tailwind CSS is loaded as JavaScript.

## Updating Libraries

To update the libraries, run from the project root:

```bash
# HTMX
curl -o src/main/resources/static/js/htmx.min.js https://unpkg.com/htmx.org@2.0.4/dist/htmx.min.js

# Tailwind CSS
curl -L -o src/main/resources/static/js/tailwind.min.js https://cdn.tailwindcss.com/3.4.1

# Lucide Icons
curl -L -o src/main/resources/static/js/lucide.min.js https://unpkg.com/lucide@0.454.0/dist/umd/lucide.min.js

# Ace Editor
curl -L -o src/main/resources/static/js/ace.js https://cdnjs.cloudflare.com/ajax/libs/ace/1.32.2/ace.js
curl -L -o src/main/resources/static/js/mode-json.js https://cdnjs.cloudflare.com/ajax/libs/ace/1.32.2/mode-json.js
curl -L -o src/main/resources/static/js/theme-github.js https://cdnjs.cloudflare.com/ajax/libs/ace/1.32.2/theme-github.js
curl -L -o src/main/resources/static/js/worker-json.js https://cdnjs.cloudflare.com/ajax/libs/ace/1.32.2/worker-json.js
```

## Notes

- All libraries are minified to optimize size
- The frontend now works completely offline
- Static routes are served from `/static/*`
- All libraries are open source and compatible with commercial use

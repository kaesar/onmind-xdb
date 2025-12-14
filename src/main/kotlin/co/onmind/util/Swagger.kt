package co.onmind.util

object Swagger {
    fun ui(): String {
        val yamlContent = this::class.java.getResourceAsStream("/swagger.yml")?.bufferedReader()?.readText() ?: ""
        return """
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>OnMind-XDB API Documentation</title>
            <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui.css" />
        </head>
        <body>
            <div id="swagger-ui"></div>
            <script src="https://unpkg.com/js-yaml@4.1.0/dist/js-yaml.min.js"></script>
            <script src="https://unpkg.com/swagger-ui-dist@5.11.0/swagger-ui-bundle.js"></script>
            <script>
                window.onload = function() {
                    const yamlText = document.getElementById('swagger-spec').textContent;
                    const spec = jsyaml.load(yamlText);
                    SwaggerUIBundle({
                        spec: spec,
                        dom_id: '#swagger-ui',
                        deepLinking: true,
                        presets: [
                            SwaggerUIBundle.presets.apis,
                            SwaggerUIBundle.SwaggerUIStandalonePreset
                        ]
                    });
                };
            </script>
            <script id="swagger-spec" type="text/yaml">
$yamlContent
            </script>
        </body>
        </html>
        """.trimIndent()
    }
}

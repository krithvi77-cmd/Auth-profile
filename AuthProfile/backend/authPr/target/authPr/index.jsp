<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Auth Profile API</title>
    <style>
        * { box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
            margin: 0;
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #fff;
        }
        .card {
            background: rgba(255,255,255,0.08);
            backdrop-filter: blur(10px);
            border: 1px solid rgba(255,255,255,0.15);
            border-radius: 14px;
            padding: 2.5rem 3rem;
            max-width: 640px;
            width: 92%;
            box-shadow: 0 20px 60px rgba(0,0,0,0.25);
        }
        h1 { margin: 0 0 .25rem; font-size: 1.85rem; }
        .tag {
            display: inline-block;
            background: #22c55e;
            color: #04261a;
            padding: 2px 10px;
            border-radius: 999px;
            font-size: .75rem;
            font-weight: 700;
            margin-bottom: 1rem;
        }
        p { line-height: 1.55; opacity: .92; }
        h3 { margin-top: 1.5rem; margin-bottom: .5rem; font-size: 1rem; opacity: .85; }
        ul { list-style: none; padding: 0; margin: 0; }
        li {
            background: rgba(0,0,0,0.2);
            padding: .6rem .9rem;
            border-radius: 8px;
            margin-bottom: .4rem;
            font-family: "SF Mono", Menlo, Consolas, monospace;
            font-size: .88rem;
            display: flex;
            gap: .75rem;
            align-items: center;
        }
        .m {
            font-size: .7rem;
            font-weight: 700;
            padding: 2px 8px;
            border-radius: 4px;
            background: #3b82f6;
        }
        .m.post { background: #f59e0b; color: #1f1300; }
        .m.put  { background: #a855f7; }
        .m.del  { background: #ef4444; }
        a { color: #fff; }
        .foot { margin-top: 1.5rem; font-size: .8rem; opacity: .7; }
    </style>
</head>
<body>
    <div class="card">
        <span class="tag">ONLINE</span>
        <h1>Auth Profile API</h1>
        <p>Backend service for managing authentication profiles (Basic Auth, OAuth v2). This is a JSON API — there is no web UI at this endpoint.</p>

        <h3>Endpoints</h3>
        <ul>
            <li><span class="m">GET</span> <a href="api/profiles">/api/profiles</a> &nbsp; — list all profiles</li>
            <li><span class="m">GET</span> /api/profiles/{id} &nbsp; — get a profile</li>
            <li><span class="m post">POST</span> /api/profiles &nbsp; — create a profile</li>
            <li><span class="m put">PUT</span> /api/profiles/{id} &nbsp; — update a profile</li>
            <li><span class="m del">DELETE</span> /api/profiles/{id} &nbsp; — delete a profile</li>
        </ul>

        <div class="foot">
            Server time: <%= new java.util.Date() %>
        </div>
    </div>
</body>
</html>

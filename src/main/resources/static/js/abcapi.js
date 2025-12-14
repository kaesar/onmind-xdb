/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 2021-06-28.
 * ABC = Articulable Backend Client
 */

function request(data) {
    return {
        method: 'POST',
        headers: { 'Content-type': 'application/json' },
        body: JSON.stringify(data)
    }
}

function response(json) {
    return json
        .then(async (response) => {
            const jsonData = await response.json();
            if (!response.ok) {
                console.error('Server error:', jsonData);
                return { ok: false, status: response.status, message: jsonData.message || jsonData.error || response.statusText };
            }
            let result = jsonData;
            if (Array.isArray(jsonData) || jsonData.id)
                result = { ok: true, status: 200, message: 'OK', total: jsonData.length || 1, data: jsonData }
            return result;
        })
        .catch((error) => {
            console.error('Request error:', error);
            return { ok: false, status: 400, message: error.message };
        });
}

export function sheet(url) {
    let data = {
        way: 'sql',
        what: 'find',
        from: 'xykit',
        some: 'sheet',
        show: 'kit01 sheetid, kit02 name, kit03 title, kit05 model'
    }
    const json = fetch(url, request(data));
    return response(json);
}

export function whoami(url) {
    let data = { call: 'whoami' }
    const json = fetch(url, request(data));
    return response(json);
}

export function signup(url, datax) {
    datax.call = 'signup';
    if (!datax.with) datax.with = 'USER';
    const json = fetch(url, request(datax));
    return response(json);
}

export function find(url, datax) {
    datax.what = 'find';
    if (datax.puts) delete datax.puts;
    const json = fetch(url, request(datax));
    return response(json);
}

export function insert(url, datax) {
    datax.what = 'insert';
    const [ok, data] = parseABC(datax);
    if (!ok)
        return { ok: false, status: 'ERROR', message: 'Wrong Request, please check it!', data };
    const json = fetch(url, request(data));
    return response(json);
}

export function update(url, datax) {
    datax.what = 'update';
    const [ok, data] = parseABC(datax);
    if (!ok)
        return { ok: false, status: 'ERROR', message: 'Wrong Request, please check it!', data };
    const json = fetch(url, request(data));
    return response(json);
}

export function remove(url, datax) {  // delete
    datax.what = 'delete';
    if (datax.puts) delete datax.puts;
    const json = fetch(url, request(datax));
    return response(json);
}

export function create(url, datax) {
    datax.what = 'create';
    if (datax.puts) delete datax.puts;
    if (datax.show) delete datax.show;
    const json = fetch(url, request(datax));
    return response(json);
}

export function drop(url, datax) {
    datax.what = 'drop';
    if (datax.puts) delete datax.puts;
    const json = fetch(url, request(datax));
    return response(json);
}

export function define(url, datax) {
    if (datax.puts && datax.puts !== '[]') {
        const spec = checkSpec(datax.puts);
        if (!spec.ok) {
            return Promise.resolve({ ok: false, status: 400, message: spec.message });
        }
    }
    
    datax.what = 'define';
    const json = fetch(url, request(datax));
    return response(json);
}

function checkSpec(spec) {
    if (!spec || spec === '[]') return { ok: true };
    
    const pattern = /^[a-z0-9]+=\w+(?:,[a-z0-9]+=\w+)*$/;
    if (!pattern.test(spec)) {
        return { ok: false, message: 'Invalid format. Use: column=alias,column2=alias2' };
    }
    
    const pairs = spec.split(',');
    const seen = new Set();
    for (const pair of pairs) {
        const [col, alias] = pair.split('=');
        if (seen.has(col)) {
            return { ok: false, message: `Duplicate column: ${col}` };
        }
        if (seen.has(alias)) {
            return { ok: false, message: `Duplicate alias: ${alias}` };
        }
        seen.add(col);
        seen.add(alias);
    }
    
    return { ok: true };
}

export function ask(url, datax) {
    let what = datax.what
    if (what == 'find')
        return this.find(url, datax)
    else if (what == 'insert')
        return this.insert(url, datax)
    else if (what == 'update')
        return this.update(url, datax)
    else if (what == 'delete')
        return this.remove(url, datax)
    else if (what == 'create')
        return this.create(url, datax)
    else if (what == 'drop')
        return this.drop(url, datax)
    else if (what == 'define')
        return this.define(url, datax)
    else {
        const json = fetch(url, request(datax));
        return response(json);    
    }
}

function parseABC(data) {
    if (data.puts && typeof(data.puts) === 'object') {
        data.puts = JSON.stringify(data.puts);
    }
    return [true, data];
}

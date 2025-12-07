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
    datax.what = 'define';
    const json = fetch(url, request(datax));
    return response(json);
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

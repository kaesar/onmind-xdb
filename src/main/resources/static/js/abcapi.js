/**
 * Created by Cesar Andres Arcila Buitrago from Colombia on 28/06/21.
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
        .then((response) => {
            return response.json();
        })
        .then((jsonData) => {
            let result = jsonData;
            if (Array.isArray(jsonData) || jsonData.id)
                result = { success: true, status: 200, message: 'OK', total: jsonData.length || 1, data: jsonData }
            return result;
        })
        .catch((error) => {
            console.error(error.message);
            return { success: false, status: 500, message: error.message };
        });
}

export function sheet(url) {
    let data = {
        way: 'sql',
        what: 'find',
        from: 'xykit',
        some: 'sheet',
        show: 'kit01 sheet, kit03 label'
    }
    const json = fetch(url, request(data));
    return response(json);
}

export function whoami(url) {
    let data = { call: 'whoami' }
    const json = fetch(url, request(data));
    return response(json);
}

export function setup(url) {
    let data = { call: 'setup' }
    const json = fetch(url, request(data));
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
        return { success: false, status: 'ERROR', message: 'Wrong Request, please check it!', data };
    const json = fetch(url, request(data));
    return response(json);
}

export function update(url, datax) {
    datax.what = 'update';
    const [ok, data] = parseABC(datax);
    if (!ok)
        return { success: false, status: 'ERROR', message: 'Wrong Request, please check it!', data };
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
    else {
        const json = fetch(url, request(datax));
        return response(json);    
    }
}

function parseABC(data) {
    if (data.puts && typeof(data.puts) === 'object') {
        data.puts = JSON.stringify(data.puts);
        /*let how = '';
        let cast = '';
        let puts = '';
        let vary = null;
        let kind = null;
        let i = 0;

        for (let item in data.puts) {
            if (i > 0) {
                how += ',';
                cast += ',';
                puts += ';';  // use ';' to avoid mix values with ','
            }
            vary = data.puts[item];
            kind = typeof(vary);

            if (kind == 'number') {
                if (Number.isInteger(vary))
                    kind = 'int';
                else
                    kind = 'float';
            }
            else if (kind == 'string') {
                vary = vary.replace(new RegExp(/'/,'g'), '`');
                vary = vary.replace(new RegExp(/;/,'g'), ',');
            }
            
            how += item;
            cast += kind;
            puts += vary;
            i++;
        }

        data.how = how;
        data.cast = cast;
        data.puts = puts;*/
    }
    return [true, data];
}

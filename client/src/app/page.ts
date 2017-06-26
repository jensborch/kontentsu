export class Page {

    private _template: string;
    private _data = {
        content: { heading: '' }
    };

    constructor(template: string, data: any) {
        this._template = template;
        this._data = data;
    }

    get template(): string {
        return this._template;
    }

    get data(): any {
        return this._data;
    }


}
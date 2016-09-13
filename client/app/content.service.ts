import { Injectable } from '@angular/core';
import { Headers, Http } from '@angular/http';

import 'rxjs/add/operator/toPromise';

@Injectable()
export class ContentService {

    private headers = new Headers({'Content-Type': 'application/hal+json'});
    private contentUrl = 'http://localhost:9090/cdn/api/files/';  // URL to web api

    constructor(private http: Http) { }

    getPage(path: String): Promise<any> {
        return this.http.get(this.contentUrl + path, {headers: this.headers})
            .toPromise()
            .then(response => response.json())
            .catch(this.handleError);
    }

    private handleError(error: any): Promise<any> {
        return Promise.reject(error.message || error);
    }
}
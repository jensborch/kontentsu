import { Injectable } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';
import { environment } from '../environments/environment';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import { Page } from './page';
import { Logger } from './logger.service';
import { Title } from '@angular/platform-browser';

@Injectable()
export class ContentService {

    private page: Page;
    private subject: Subject<Page> = new Subject<Page>();

    private headers = new Headers({
        'Content-Type': 'application/json'
    });

    constructor(private http: Http, private log: Logger, private titleService: Title, ) { }

    load(path?: String): void {
        if (!path) {
            path = environment.frontPage;
        }
        this.http.get(environment.filesApi + path, { headers: this.headers }).subscribe(p => {
            let json = p.json();
            if (json.content && json.content.heading) {
                this.log.info('Setting heading from content "' + json.content.heading + '"');
                this.titleService.setTitle(json.content.heading);
            }
            if (json.template && json.template.href) {
                this.log.info('Setting template from content ' + json.template.href);
                this.page = new Page(json.template.href, json);
            } else {
                this.log.info('Using default template ' + environment.defaultTemplate);
                this.page = new Page(environment.defaultTemplate, json);
            }
            this.subject.next(this.page);
        }, e => {
            this.log.error('Error getting front page at ' + environment.frontPage);
            this.subject.error(this.page);
        });
    }

    getPage(): Observable<Page> {
        return this.subject.asObservable();
    }
}

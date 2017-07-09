import { Injectable } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';
import { environment } from '../../environments/environment';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import { Page } from '../page';
import { Logger } from '../logger.service';
import { Title } from '@angular/platform-browser';

@Injectable()
export class ContentService {

    private page: Page;
    private subject: Subject<Page> = new Subject<Page>();

    private headers = new Headers({
        'Content-Type': 'application/json'
    });

    constructor(private http: Http,
        private log: Logger,
        private titleService: Title) { }

    load(path?: String): void {
        if (!path) {
            path = environment.frontPage;
        }
        const pageUrl = environment.filesApi + path;
        this.http.get(pageUrl, { headers: this.headers })
            .map((res: Response) => res.json())
            .subscribe(p => {
                if (p.content && p.content.heading) {
                    this.log.info('Setting heading from content "' + p.content.heading + '"');
                    this.titleService.setTitle(p.content.heading);
                }
                if (p.template && p.template.href) {
                    this.log.info('Setting template from content ' + p.template.href);
                    this.page = new Page(p.template.href, p);
                } else {
                    this.log.info('Using default template ' + environment.defaultTemplate);
                    this.page = new Page(environment.defaultTemplate, p);
                }
                this.subject.next(this.page);
            }, e => {
                this.subject.error('Error getting page at ' + pageUrl);
            });
    }

    getPage(): Observable<Page> {
        return this.subject.asObservable();
    }
}
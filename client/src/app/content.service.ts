import { Injectable } from '@angular/core';
import { Headers, Http, Response } from '@angular/http';
import { environment } from '../environments/environment';
import { Subject } from 'rxjs/Subject';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/catch';
import 'rxjs/add/operator/map';
import 'rxjs/add/observable/throw';
import { Page } from './page';
import { Logger } from './logger.service';
import { Title } from '@angular/platform-browser';
import { Location } from '@angular/common';

@Injectable()
export class ContentService {

    private page: Page;
    private subject: Subject<Page> = new Subject<Page>();

    private headers = new Headers({
        'Content-Type': 'application/json'
    });

    constructor(private http: Http, private log: Logger, private titleService: Title, private location: Location) {
        location.subscribe((l) => {
            if (location.path() !== '') {
                this.load(location.path());
            }
        });
    }

    load(path?: String): void {
        if (!path) {
            path = environment.frontPage;
        }
        this.log.error('hej');
        this.http.get(environment.filesApi + path, { headers: this.headers })
            .map((res: Response) => res.json())
            .catch((error: any, c: Observable<Response>) => {
                this.subject.error('Error getting front page at ' + environment.frontPage);
                return Observable.throw(error.json().error || 'Server error');
            })
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
            });
    }

    private handleError(error: Response | any) {
        let msg: string;
        if (error instanceof Response) {
            const body = error.json() || '';
            const err = body.error || JSON.stringify(body);
            msg = `${error.status} - ${error.statusText || ''} ${err}`;
        } else {
            msg = error.message ? error.message : error.toString();
        }
        return Observable.throw(msg);
    }

    getPage(): Observable<Page> {
        return this.subject.asObservable();
    }
}

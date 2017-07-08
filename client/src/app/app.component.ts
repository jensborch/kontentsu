import { Component, OnInit, Inject, } from '@angular/core';
import { ContentService } from './content.service';
import { Logger } from './logger.service';
import { DOCUMENT } from '@angular/platform-browser';
import { environment } from '../environments/environment';
import { Page } from './page';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/observable/throw';

@Component({
    selector: 'k-app',
    template: '<k-template></k-template>'
})
export class AppComponent implements OnInit {

    private loaded = false;

    constructor(
        @Inject(DOCUMENT) private doc: any,
        private contentService: ContentService,
        private log: Logger) { }


    ngOnInit(): void {
        this.contentService.load();
        this.contentService.getPage()
            .catch((error: any, c: any) => {
                this.log.error('Error getting front page at ' + environment.frontPage);
                this.doc.dispatchEvent(new CustomEvent('apperror', {}));
                return Observable.throw('Error getting front page at ' + environment.frontPage);
            })
            .subscribe(p => {
                if (!this.loaded) {
                    this.doc.dispatchEvent(new CustomEvent('appready', {}));
                    this.loaded = true;
                }
            });
    }
}

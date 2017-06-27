import { Component, OnInit, Inject, } from '@angular/core';
import { ContentService } from './content.service';
import { Logger } from './logger.service';
import { DOCUMENT } from '@angular/platform-browser';
import { environment } from '../environments/environment';
import { Page } from './page';

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
        this.contentService.getPage().subscribe(p => {
            if (!this.loaded) {
                this.doc.dispatchEvent(new CustomEvent('appready', {}));
                this.loaded = true;
            }
        }, e => {
            this.log.error('Error getting front page at ' + environment.frontPage);
            this.doc.dispatchEvent(new CustomEvent('apperror', {}));
        });
    }
}

import { Component, OnInit, Inject } from '@angular/core';
import { Location, PopStateEvent } from '@angular/common';
import { ContentService } from './content.service';
import { Logger } from './logger.service';
import { DOCUMENT } from '@angular/platform-browser';
import { environment } from '../environments/environment';
import { Page } from './page';
import {Router} from '@angular/router';

@Component({
    selector: 'k-app',
    template: '<k-template></k-template>'
})
export class AppComponent implements OnInit {

    private loaded = false;

    constructor(
        @Inject(DOCUMENT) private doc: any,
        private contentService: ContentService,
        private location: Location,
        private router: Router,
        private log: Logger) { }


    ngOnInit(): void {
        this.router.events.subscribe((e: any) => {
            if (e.url && e.url !== '', e.url !== '/') {
                this.contentService.load(e.url.slice(1) + '/');
            }
        });
        if (this.location.path(false) !== '') {
            this.contentService.load(this.location.path(false).slice(1) + '/');
        } else {
            this.contentService.load();
        }
        this.contentService.getPage()
            .subscribe(p => {
                if (!this.loaded) {
                    this.doc.dispatchEvent(new CustomEvent('appready', {}));
                    this.loaded = true;
                }
            }, e => {
                this.log.error('Error getting front page "' + environment.frontPage + '"');
                this.doc.dispatchEvent(new CustomEvent('apperror', {}));
            });
    }
}

import { Component, OnInit, Inject } from '@angular/core';
import { ContentService } from './content/content.service';
import { Logger } from './logger.service';
import { DOCUMENT } from '@angular/platform-browser';
import { environment } from '../environments/environment';
import { Page } from './page';
import { Router, Routes, NavigationStart } from '@angular/router';
import 'rxjs/add/operator/filter';

@Component({
    selector: 'k-app',
    template: '<k-template></k-template>'
})
export class AppComponent implements OnInit {

    private loaded = false;

    constructor(
        @Inject(DOCUMENT) private doc: any,
        private contentService: ContentService,
        private router: Router,
        private log: Logger) { }


    ngOnInit(): void {
        this.router.events
            .filter(e => e instanceof NavigationStart)
            .subscribe((s: NavigationStart) => {
                if (s.url !== '', s.url !== '/') {
                    this.contentService.load(s.url.slice(1) + '/');
                    this.router.config.concat({
                        path: s.url,
                        component: AppComponent
                    });
                } else {
                    this.contentService.load();
                }
            });
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

import { Component, OnInit, Inject } from '@angular/core';
import { ContentService } from './content.service';
import { TemplateComponent } from './template/template.component';
import { Logger } from './logger.service';
import { BrowserModule, Title } from '@angular/platform-browser';
import { DOCUMENT } from '@angular/platform-browser';
import { environment } from '../environments/environment';

@Component({
    selector: 'k-app',
    template: '<k-template *ngIf="page.content" [url]="template" [data]="page"></k-template>'
})
export class AppComponent implements OnInit {

    page = {
        content: { heading: '' }
    };

    constructor(
        @Inject(DOCUMENT) private doc: any,
        private contentService: ContentService,
        private titleService: Title,
        private log: Logger) { }


    ngOnInit(): void {
        this.contentService.getPage(environment.frontPage).then(p => {
            this.page = p;
            if (p.content && p.content.heading) {
                this.log.info('Setting heading from content "' + p.content.heading + '"');
                this.titleService.setTitle(p.content.heading);
            }
            if (p.template && p.template.href) {
                this.log.info('Setting template from content ' + p.template.href);
                environment.defaultTemplate = p.template.href;
            }
            this.log.info('Using template ' + environment.defaultTemplate);
            this.doc.dispatchEvent(new CustomEvent('appready', {}));
        });
    }
}

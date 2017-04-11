import { Component, OnInit } from '@angular/core';
import { ContentService } from './content.service';
import { TemplateComponent } from './template.component';
import { Logger } from './logger.service';
import { BrowserModule, Title } from '@angular/platform-browser';
@Component({
    selector: 'k-app',
    templateUrl: 'app.component.html'
})
export class AppComponent implements OnInit {
    frontPage: String = 'pages/page-simple/page-simple';
    template: String = 'templates/responsive-one-article.tpl.html';
    page = {
        content: { heading: 'Test title' }
    };

    constructor(private contentService: ContentService, private titleService: Title, private log: Logger) { }

    ngOnInit(): void {
        this.contentService.getPage(this.frontPage).then(p => {
            this.page = p;
            if (p.content && p.content.heading) {
                this.log.info('Setting heading "' + p.content.heading + '"');
                this.titleService.setTitle(p.content.heading);
            }
            if (p.template && p.template.href) {
                this.log.info('Setting template ' + p.template.href);
                this.template = p.template.href;
            }
            this.log.info('Using template ' + this.template);
        });
    }
}

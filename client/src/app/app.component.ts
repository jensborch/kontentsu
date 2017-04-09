import { Component, OnInit } from '@angular/core';
import { ContentService } from './content.service';
import { TemplateComponent } from './template.component';
import { BrowserModule, Title } from '@angular/platform-browser';
@Component({
    selector: 'editing-app',
    templateUrl: 'app.component.html'
})
export class AppComponent implements OnInit {
    frontPage: String = 'pages/page-simple/page-simple';
    template: String = 'templates/responsive-one-article.tpl.html';
    page = { };

    constructor(private contentService: ContentService, private titleService: Title) { }

    ngOnInit(): void {
        this.contentService.getPage(this.frontPage).then(p => {
            console.info(p);
            this.page = p
            if (p.content && p.content.heading) {
                console.info("Setting heading " + p.content.heading);
                this.titleService.setTitle(p.content.heading);
            }
             console.info("hej1");
            if (p.template && p.template.href) {
                console.info("Setting template " + p.template.href);
                this.template = p.template.href;
            }
            console.info("Using template " + this.template);
        });
    }
}
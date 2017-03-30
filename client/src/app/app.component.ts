import { Component, OnInit } from '@angular/core';
import { ContentService } from './content.service';
@Component({
    selector: 'editing-app',
    templateUrl: 'app.component.html',
    providers: [ContentService]
})
export class AppComponent implements OnInit {
    frontPage: String = 'pages/page-simple/page-simple';
    page = {};

    constructor(private contentService: ContentService) { }

    ngOnInit(): void {
        this.contentService.getPage(this.frontPage).then(p => {
            this.page = p
            //this.templateUrl = p.template.href;
        });
    }

}
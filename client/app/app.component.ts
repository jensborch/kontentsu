import { Component, OnInit } from '@angular/core';
import { ContentService } from './content.service';
@Component({
    selector: 'editing-app',
    templateUrl: 'app/app.component.html',
    providers: [ContentService]
})
export class AppComponent implements OnInit {
    private frontPage: String = 'pages/page-simple/page-simple';
    page = {};

    constructor(private contentService: ContentService) { }

    ngOnInit(): void {
        this.getPage(this.frontPage);
    }

    getPage(path: String): void {
        this.contentService.getPage(path).then(page => this.page = page);
    }

}
import { Component } from '@angular/core';
import { ContentService } from './content.service';
@Component({
    selector: 'editing-app',
    template: '<h1>Kontentsu editing application</h1>',
    providers: [ContentService]
})
export class AppComponent {
    page = {}; 

    constructor(private contentService: ContentService) { }

   getPage(): void {
    this.contentService.getPage().then(page => this.page = page);
  }

}
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article/article.component';
import { MdCardModule, MdMenuModule, MdIconModule, MdToolbarModule } from '@angular/material';
//import { FlexLayoutModule, MediaMonitor } from "@angular/flex-layout";


@NgModule({
    imports: [CommonModule,  MdCardModule, MdMenuModule, MdIconModule, MdToolbarModule],
    declarations: [ArticleComponent],
    providers: [],
    exports: [ArticleComponent, MdCardModule, MdMenuModule, MdIconModule, MdToolbarModule]
})
export class PageModule { }

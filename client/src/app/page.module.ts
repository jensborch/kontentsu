import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article/article.component';
import { MaterialModule } from '@angular/material';
import { MdCardModule, MdSidenavModule, MdIconModule, MdGridListModule, MdListModule, MdToolbarModule } from '@angular/material';
import { FlexLayoutModule } from "@angular/flex-layout";


@NgModule({
    imports: [CommonModule, FlexLayoutModule],
    declarations: [ArticleComponent],
    exports: [ArticleComponent, MdCardModule, MdSidenavModule, MdIconModule, MdGridListModule, MdListModule, MdToolbarModule]
})
export class PageModule { }

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article/article.component';
import { ContentComponent } from './content/content.component';
import { MdCardModule, MdMenuModule, MdIconModule, MdToolbarModule } from '@angular/material';
import { RouterModule } from '@angular/router';

@NgModule({
    imports: [CommonModule, RouterModule, MdCardModule, MdMenuModule, MdIconModule, MdToolbarModule],
    declarations: [ArticleComponent, ContentComponent],
    providers: [],
    exports: [ArticleComponent, ContentComponent, RouterModule, MdCardModule, MdMenuModule, MdIconModule, MdToolbarModule]
})
export class PageModule { }

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article/article.component';

@NgModule({
    imports: [CommonModule],
    declarations: [ArticleComponent],
    exports: [ArticleComponent]
})
export class PageModule { }

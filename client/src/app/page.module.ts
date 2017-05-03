import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article/article.component';
import { MaterialModule} from '@angular/material';
import {MdCardModule} from '@angular/material';

@NgModule({
    imports: [CommonModule],
    declarations: [ArticleComponent],
    exports: [ArticleComponent, MdCardModule]
})
export class PageModule { }

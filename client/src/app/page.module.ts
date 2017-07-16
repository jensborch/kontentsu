import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article/article.component';
import { ContentComponent } from './content/content.component';
import { MdCardModule, MdMenuModule, MdIconModule, MdToolbarModule } from '@angular/material';
import { RouterModule } from '@angular/router';
import { LinkDirective } from 'app/link/link.directive';
import { FlexLayoutModule } from '@angular/flex-layout';

@NgModule({
    imports: [
        CommonModule,
        FlexLayoutModule,
        RouterModule,
        MdCardModule,
        MdMenuModule,
        MdIconModule,
        MdToolbarModule],
    declarations: [ArticleComponent, ContentComponent, LinkDirective],
    providers: [],
    exports: [
        ArticleComponent,
        ContentComponent,
        LinkDirective,
        CommonModule,
        FlexLayoutModule,
        RouterModule,
        MdCardModule,
        MdMenuModule,
        MdIconModule,
        MdToolbarModule]
})
export class PageModule { }

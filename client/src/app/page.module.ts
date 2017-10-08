import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArticleComponent } from './article/article.component';
import { ContentComponent } from './content/content.component';
import { MatCardModule, MatMenuModule, MatIconModule, MatToolbarModule } from '@angular/material';
import { RouterModule } from '@angular/router';
import { LinkDirective } from 'app/link/link.directive';
import { FlexLayoutModule } from '@angular/flex-layout';

@NgModule({
    imports: [
        CommonModule,
        FlexLayoutModule,
        RouterModule,
        MatCardModule,
        MatMenuModule,
        MatIconModule,
        MatToolbarModule],
    declarations: [ArticleComponent, LinkDirective, ContentComponent],
    providers: [],
    exports: [
        ArticleComponent,
        ContentComponent,
        LinkDirective,
        CommonModule,
        FlexLayoutModule,
        RouterModule,
        MatCardModule,
        MatMenuModule,
        MatIconModule,
        MatToolbarModule]
})
export class PageModule { }

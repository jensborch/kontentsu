import { Component, Input } from '@angular/core';
import { ContentComponent } from '../content/content.component';
import { environment } from '../../environments/environment';


@Component({
  selector: 'k-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.css']
})
export class ArticleComponent {
  @Input() data = {};

  root: string;

  constructor() {
    this.root = environment.filesApi;
  }

}

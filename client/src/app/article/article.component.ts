import { Component, Input } from '@angular/core';
import { ContentComponent } from '../content/content.component';


@Component({
  selector: 'k-article',
  templateUrl: './article.component.html',
  styleUrls: ['./article.component.css']
})
export class ArticleComponent {
  @Input() data = {};

  constructor() { }

}

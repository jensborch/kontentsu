import { OnInit, NgModule, Component, ViewContainerRef, AfterViewInit, Compiler, ComponentRef, ViewChild } from '@angular/core';
import { Http, Response } from '@angular/http';
import { CommonModule } from '@angular/common';
import { PageModule } from '../page.module';
import { ArticleComponent } from '../article/article.component';
import { Logger} from '../logger/logger.service';
import { Page } from './../page';
import { ContentService } from '../content/content.service';
import { Observable } from 'rxjs/Observable';

@Component({
    selector: 'k-template',
    template: '<ng-template #target></ng-template>'
})
export class TemplateComponent implements OnInit {

    @ViewChild('target', { read: ViewContainerRef }) target: ViewContainerRef;

    private page: Page;

    constructor(
        private contentService: ContentService,
        private http: Http,
        private compiler: Compiler,
        private log: Logger) { }

    ngOnInit(): void {
        this.contentService.getPage().subscribe((p: Page) => {
            this.page = p;
            this.loadTemplate();
        });
    }

    private createComponent(template: string, data) {

        @Component({
            selector: 'k-dynamic-template',
            template: template,
            styleUrls: ['template.component.scss']
        })
        class DynamicTemplateComponent {
            page = {};
        };

        @NgModule({
            imports: [CommonModule, PageModule],
            declarations: [DynamicTemplateComponent]
        })
        class DynamicTemplateModule { }

        this.compiler.compileModuleAndAllComponentsAsync(DynamicTemplateModule)
            .then(factory => {
                const compFactory = factory.componentFactories.find(x => x.componentType === DynamicTemplateComponent);
                this.target.clear();
                const cmpRef: ComponentRef<DynamicTemplateComponent> = this.target
                    .createComponent(compFactory);
                cmpRef.instance.page = data;
            });
    }

    private loadTemplate() {
        this.http.get(this.page.template)
            .subscribe((res: Response) => {
                this.log.info('Template: ' + this.page.template);
                const template = res.text();
                this.log.debug('Template data: ' + template);
                this.createComponent(template, this.page.data);
            }, e => {
                this.log.error('Error getting template ' + this.page.template);
            });
    }

}

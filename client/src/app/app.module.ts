import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpModule } from '@angular/http';

import { AppComponent } from './app.component';
import { TemplateComponent } from './template/template.component';
import { ContentService } from './content.service';
import { Logger } from './logger.service';
import { MaterialModule } from '@angular/material';

@NgModule({
  declarations: [
    AppComponent,
    TemplateComponent
  ],
  imports: [
    BrowserModule,
    FormsModule,
    HttpModule,
    MaterialModule
  ],
  providers: [ContentService, Logger],
  bootstrap: [AppComponent]
})
export class AppModule { }

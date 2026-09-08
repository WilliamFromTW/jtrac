/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.util;

import info.jtrac.domain.AbstractItem;
import info.jtrac.domain.ColumnHeading;
import info.jtrac.domain.ColumnHeading.Name;
import info.jtrac.domain.Field;
import info.jtrac.domain.History;
import info.jtrac.domain.ItemSearch;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.*;

/**
 * Excel Sheet generation helper utility
 */
public class ExcelUtils {
    private Sheet sheet;
    private List<AbstractItem> items;
    private ItemSearch itemSearch;
    private CellStyle csBold;
    private CellStyle csDate;
    private Workbook wb;

    public ExcelUtils (List items, ItemSearch itemSearch) {
		try {
			this.wb = WorkbookFactory.create(false);
			this.sheet = wb.createSheet("jtrac");
			this.sheet.setDefaultColumnWidth((short) 12);

			Font fBold = wb.createFont();
			fBold.setBold(true);
			this.csBold = wb.createCellStyle();
			this.csBold.setFont(fBold);

			this.csDate = wb.createCellStyle();
			this.csDate.setDataFormat((short) BuiltinFormats.getBuiltinFormat("m/d/yy"));

			this.items = items;
			this.itemSearch = itemSearch;
		} catch (IOException ioex) { /* shouldn't happen */ }
	}

    private Cell getCell(int row, int col) {
        Row sheetRow = sheet.getRow(row);
        if (sheetRow == null) {
            sheetRow = sheet.createRow(row);
        }
        Cell cell = sheetRow.getCell((short) col);
        if (cell == null) {
            cell = sheetRow.createCell((short) col);
        }
        return cell;
    }

    private void setText(int row, int col, String text) {
        Cell cell = getCell(row, col);
        cell.setCellValue(text);
    }

    private void setDate(int row, int col, Date date) {
        if (date == null) {
            return;
        }
        Cell cell = getCell(row, col);
        cell.setCellValue(date);
        cell.setCellStyle(csDate);
    }

    private void setDouble(int row, int col, Double value) {
        if (value == null) {
            return;
        }
        Cell cell = getCell(row, col);
        cell.setCellValue(value);
    }

    private void setHeader(int row, int col, String text) {
        Cell cell = getCell(row, col);
        cell.setCellStyle(csBold);
        cell.setCellValue(text);
    }

    public Workbook exportToExcel(Map<Name, String> localizedLabels) {

        boolean showHistory = itemSearch.isShowHistory();
        List<ColumnHeading> columnHeadings = itemSearch.getColumnHeadingsToRender();

        int row = 0;
        int col = 0;

        // begin header row
        for(ColumnHeading ch : columnHeadings) {
            setHeader(row, col++, ch.isField() ? ch.getLabel() : localizedLabels.get(ch.getName()));
        }

        // iterate over list
        for(AbstractItem item : items) {
            row++;
            col = 0;
            for(ColumnHeading ch : columnHeadings) {
                if(ch.isField()) {
                    Field field = ch.getField();
                    switch(field.getName().getType()) {
                        case 4: // double
                            setDouble(row, col++, (Double) item.getValue(field.getName()));
                            break;
                        case 6: // date
                            setDate(row, col++, (Date) item.getValue(field.getName()));
                            break;
                        default:
                            setText(row, col++, item.getCustomValue(field.getName()));
                    }
                } else {
                    switch(ch.getName()) {
                        case ID:
                            if (showHistory) {
                                int index = ((History) item).getIndex();
                                if (index > 0) {
                                    setText(row, col++, item.getRefId() + " (" + index + ")");
                                } else {
                                    setText(row, col++, item.getRefId());
                                }
                            } else {
                                setText(row, col++, item.getRefId());
                            }
                            break;
                        case SUMMARY:
                            setText(row, col++, item.getSummary());
                            break;
                        case DETAIL:
                            if (showHistory) {
                                History h = (History) item;
                                if(h.getIndex() > 0) {
                                    setText(row, col++, h.getComment());
                                } else {
                                    setText(row, col++, h.getDetail());
                                }
                            } else {
                                setText(row, col++, item.getDetail());
                            }
                            break;
                        case LOGGED_BY:
                            setText(row, col++, item.getLoggedBy().getName());
                            break;
                        case STATUS:
                            setText(row, col++, item.getStatusValue());
                            break;
                        case ASSIGNED_TO:
                            setText(row, col++, (item.getAssignedTo() == null ? "" : item.getAssignedTo().getName()));
                            break;
                        case TIME_STAMP:
                        case LAST_CHANGED:
                            setDate(row, col++, item.getTimeStamp());
                            break;
                        case SPACE:
                            setText(row, col++, item.getSpace().getName());
                            break;
                        default:
                            throw new RuntimeException("Unexpected name: '" + ch.getName() + "'");
                    }
                }
            }
        }
        return wb;
    }
}

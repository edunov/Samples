require(quantmod)


"parseQuotes" <-
function(From)
{
    require(xts) #загрузить модуль xts, он нам нужен
    fr <- read.csv(From, as.is = TRUE) #читаем данные из файла
    fr <- xts(as.matrix(fr[,(5:9)]), as.Date(strptime(fr[,3], "%Y%m%d")))
    # функция strptime позволяет нам распарсить дату в заданном формате, в данном случае формат определяется выражением "%Y%m%d"
    # для интрадей данных последнюю строчку надо заменить на
#fr&lt;-xts(as.matrix(fr[,(4:9)]),as.POSIXct(strptime(paste(fr[,3],fr[,4]), "%Y%m%d %H%M%S")))
    colnames(fr) <- c('Open','High','Low','Close','Volume') #присваивамем новые имена колонам для совместимости с xts
    return(fr)
}



"testRSI" <- function(data, period = 7, high = 80, low = 20){

require(quantmod)

rsi = RSI(Cl(data), n=period) #К счастью для нас индикатор RSI уже реализован в модуле TTR

ymin=min(Lo(data))
ymax=max(Lo(data))


par(mfrow=c(2,2), oma=c(2,2,2,2))

plot(Cl(data),ylim=c(ymin,ymax), pch=15,main="Buy & Hold")
lines(Cl(data), c(ymin,ymax))


sigup <-ifelse(rsi < low,1,0)
sigdn <-ifelse(rsi > high,-1,0)


sigup <- lag(sigup,1)
sigdn <- lag(sigdn,1)

sigup[is.na(sigup)] <- 0
sigdn[is.na(sigdn)] <- 0

sig <- sigup + sigdn

ret <- ROC(Cl(data))
ret[1] <- 0

eq_up <- cumprod(1+ret*sigup)
eq_dn <- cumprod(1+ret*sigdn)
eq_all <- cumprod(1+ret*sig)



mfg=c(1,2)
plot(eq_up,main="Доходность длинных позиций",col="green")
mfg=c(2,2)
plot(eq_all,main="Общая доходность",col="blue")
mfg=c(2,1)
plot(eq_dn,main="Доходность коротких позиций",col="red")
title("Торговая система основанная на RSI (www.algorithmist.ru)", outer = TRUE)

print(paste("Средняя дневная прибыль %", mean(ret*sig)*100, mean(ret*sigup)*100, mean(ret*sigdn)*100))
print(paste("Среднеквадратичное отклонение от прибыли %", sd(ret*sig)*100, sd(ret*sigup)*100, sd(ret*sigdn)*100))
print(paste("Всего сигналов", sum(abs(sig)), sum(sigup), sum(abs(sigdn))))

print(paste("% удачных дней", sum(ifelse(ret*sig > 0 ,1,0))*100 / sum(abs(sig)) , sum(ifelse(ret*sigup>0 ,1,0))*100 / sum(sigup), sum(ifelse(ret*sigdn>0 ,1,0))*100 / sum(abs(sigdn))))

print(paste("Максимальная просадка %", max((cummax(eq_all)-eq_all)/cummax(eq_all)) *100))

income <- last(SMA(ret*sig, n=5), n=1000)

}

#Данные следует качать отсюда http://www.finam.ru/analysis/export/default.asp

MICEX <- parseQuotes("C:\\data\\MICEX_050317_110317.txt")
GAZP <- parseQuotes("C:\\data\\GAZP_050317_110317.txt")
getSymbols("^GSPC", from="2005-03-17")

testRSI(GAZP, period=7, high=70, low=30)
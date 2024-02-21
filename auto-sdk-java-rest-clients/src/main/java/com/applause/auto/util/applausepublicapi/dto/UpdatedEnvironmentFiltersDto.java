/*
 *
 * Copyright © 2024 Applause App Quality, Inc.
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
 *
 */
package com.applause.auto.util.applausepublicapi.dto;

import java.util.List;

/**
 * UpdatedEnvironmentFiltersDto
 *
 * @param alternatePayment -- GETTER -- Get alternatePayment
 * @param bankAccount -- GETTER -- Get bankAccount
 * @param car -- GETTER -- Get car
 * @param carSharingService -- GETTER -- Get carSharingService
 * @param creditAndDebitCard -- GETTER -- Get creditAndDebitCard
 * @param desktop -- GETTER -- Get desktop
 * @param energyProvider -- GETTER -- Get energyProvider
 * @param entertainment -- GETTER -- Get entertainment
 * @param ewallet -- GETTER -- Get ewallet
 * @param gamingConsole -- GETTER -- Get gamingConsole
 * @param internetServiceProvider -- GETTER -- Get internetServiceProvider
 * @param loyaltyProgram -- GETTER -- Get loyaltyProgram
 * @param mobile -- GETTER -- Get mobile
 * @param mobileWallet -- GETTER -- Get mobileWallet
 * @param motorcycle -- GETTER -- Get motorcycle
 * @param settopbox -- GETTER -- Get settopbox
 * @param smartBluRayPlayer -- GETTER -- Get smartBluRayPlayer
 * @param smartHomeDevice -- GETTER -- Get smartHomeDevice
 * @param smartTv -- GETTER -- Get smartTv
 * @param smartwatch -- GETTER -- Get smartwatch
 * @param tvProvider -- GETTER -- Get tvProvider
 * @param wearable -- GETTER -- Get wearable
 */
public record UpdatedEnvironmentFiltersDto(
    List<AlternatePaymentFilterUpdateDto> alternatePayment,
    List<BankAccountFilterUpdateDto> bankAccount,
    List<CarFilterUpdateDto> car,
    List<CarSharingServiceFilterUpdateDto> carSharingService,
    List<CreditAndDebitCardFilterUpdateDto> creditAndDebitCard,
    List<DesktopFilterUpdateDto> desktop,
    List<EnergyProviderFilterUpdateDto> energyProvider,
    List<EntertainmentFilterUpdateDto> entertainment,
    List<EWalletFilterUpdateDto> ewallet,
    List<GamingConsoleFilterUpdateDto> gamingConsole,
    List<InternetServiceProviderFilterUpdateDto> internetServiceProvider,
    List<LoyaltyProgramFilterUpdateDto> loyaltyProgram,
    List<MobileFilterUpdateDto> mobile,
    List<MobileWalletFilterUpdateDto> mobileWallet,
    List<MotorcycleFilterUpdateDto> motorcycle,
    List<SetTopBoxFilterUpdateDto> settopbox,
    List<SmartBluRayPlayerFilterUpdateDto> smartBluRayPlayer,
    List<SmartHomeDeviceFilterUpdateDto> smartHomeDevice,
    List<SmartTvFilterUpdateDto> smartTv,
    List<SmartwatchFilterUpdateDto> smartwatch,
    List<TvProviderFilterUpdateDto> tvProvider,
    List<WearableFilterUpdateDto> wearable) {}
